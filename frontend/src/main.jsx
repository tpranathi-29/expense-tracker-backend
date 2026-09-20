import { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  ArrowDownRight,
  ArrowUpRight,
  BarChart3,
  CalendarDays,
  Download,
  LogOut,
  Plus,
  Search,
  Trash2,
  Upload,
  Wallet,
  X,
} from "lucide-react";
import "./styles.css";

const configuredApiUrl = import.meta.env.VITE_API_URL?.trim();
const defaultApiUrl =
  window.location.hostname === "localhost" ||
  window.location.hostname === "127.0.0.1"
    ? "http://localhost:8080"
    : "https://expense-tracker-backend-q5a1.onrender.com";
const API_URL = (configuredApiUrl || defaultApiUrl).replace(/\/$/, "");
const categories = [
  "Food",
  "Transport",
  "Housing",
  "Health",
  "Shopping",
  "Entertainment",
  "Other",
];
const emptyForm = {
  title: "",
  amount: "",
  category: "Food",
  type: "Expense",
  date: new Date().toISOString().slice(0, 10),
  description: "",
};

async function request(path, options = {}, token) {
  const headers = {
    ...(options.body instanceof FormData
      ? {}
      : { "Content-Type": "application/json" }),
    ...(options.headers || {}),
  };
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(`${API_URL}${path}`, { ...options, headers });
  if (!response.ok)
    throw new Error((await response.text()) || "Request failed");
  return response.headers.get("content-type")?.includes("application/json")
    ? response.json()
    : response.text();
}

function money(value) {
  return new Intl.NumberFormat("en-US", {
    style: "currency",
    currency: "USD",
  }).format(Number(value || 0));
}

function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event) {
    event.preventDefault();
    setError("");
    setBusy(true);
    try {
      const payload =
        mode === "register"
          ? form
          : { email: form.email, password: form.password };
      const result = await request(`/auth/${mode}`, {
        method: "POST",
        body: JSON.stringify(payload),
      });
      if (mode === "register") {
        setMode("login");
        setError("Account created. Sign in to continue.");
        return;
      }
      if (
        typeof result !== "string" ||
        result.toLowerCase().includes("invalid") ||
        result.toLowerCase().includes("not found")
      )
        throw new Error(result);
      localStorage.setItem("ledgerly-token", result);
      onAuthenticated(result);
    } catch (err) {
      setError(err.message || "Unable to connect to the API.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <main className="auth-layout">
      <section className="auth-story">
        <span className="eyebrow">PERSONAL FINANCE, CLARIFIED</span>
        <h1>
          Your money,
          <br />
          <em>in focus.</em>
        </h1>
        <p>Track the everyday decisions that shape your bigger picture.</p>
        <div className="story-line" />
      </section>
      <section className="auth-panel">
        <div className="brand-mark">
          <Wallet size={18} /> ledgerly
        </div>
        <div className="auth-copy">
          <span className="eyebrow">WELCOME BACK</span>
          <h2>
            {mode === "login" ? "Sign in to your ledger" : "Start your ledger"}
          </h2>
          <p>
            {mode === "login"
              ? "Pick up where you left off."
              : "A calmer way to understand your spending."}
          </p>
        </div>
        <form onSubmit={submit}>
          {mode === "register" && (
            <label>
              Name
              <input
                type="text"
                required
                value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="Your name"
              />
            </label>
          )}
          <label>
            Email
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              placeholder="you@example.com"
            />
          </label>
          <label>
            Password
            <input
              type="password"
              required
              minLength="6"
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              placeholder="At least 6 characters"
            />
          </label>
          {error && <div className="form-message">{error}</div>}
          <button className="primary-button" disabled={busy}>
            {busy
              ? "Please wait..."
              : mode === "login"
                ? "Enter dashboard"
                : "Create account"}
          </button>
        </form>
        <button
          className="text-button"
          onClick={() => {
            setMode(mode === "login" ? "register" : "login");
            setError("");
          }}
        >
          {mode === "login"
            ? "New here? Create an account"
            : "Already have an account? Sign in"}
        </button>
      </section>
    </main>
  );
}

function SharedExpensesPanel({ token }) {
  const [groups, setGroups] = useState([]);
  const [selectedGroupId, setSelectedGroupId] = useState("");
  const [balances, setBalances] = useState([]);
  const [sharedExpenses, setSharedExpenses] = useState([]);
  const [groupName, setGroupName] = useState("");
  const [memberEmail, setMemberEmail] = useState("");
  const [sharedForm, setSharedForm] = useState({
    description: "",
    totalAmount: "",
    payerEmail: "",
    date: new Date().toISOString().slice(0, 10),
    shares: "",
  });
  const [settlement, setSettlement] = useState({ toUserEmail: "", amount: "" });
  const [sharingError, setSharingError] = useState("");

  async function loadGroups() {
    const result = await request("/groups", {}, token);
    setGroups(result);
    if (!selectedGroupId && result.length) setSelectedGroupId(String(result[0].id));
  }

  async function loadGroupData(groupId) {
    if (!groupId) return;
    const [nextBalances, nextExpenses] = await Promise.all([
      request(`/groups/${groupId}/balances`, {}, token),
      request(`/groups/${groupId}/expenses`, {}, token),
    ]);
    setBalances(nextBalances);
    setSharedExpenses(nextExpenses);
  }

  useEffect(() => {
    loadGroups().catch(() => setSharingError("Could not load shared groups."));
  }, [token]);

  useEffect(() => {
    loadGroupData(selectedGroupId).catch(() => setSharingError("Could not load group balances."));
  }, [selectedGroupId]);

  async function createGroup(event) {
    event.preventDefault();
    try {
      const group = await request("/groups", { method: "POST", body: JSON.stringify({ name: groupName }) }, token);
      setGroups([...groups, group]);
      setSelectedGroupId(String(group.id));
      setGroupName("");
    } catch (err) {
      setSharingError(err.message || "Could not create group.");
    }
  }

  async function addMember(event) {
    event.preventDefault();
    try {
      const group = await request(`/groups/${selectedGroupId}/members`, { method: "POST", body: JSON.stringify({ email: memberEmail }) }, token);
      setGroups(groups.map((item) => item.id === group.id ? group : item));
      setMemberEmail("");
    } catch (err) {
      setSharingError(err.message || "Could not add member.");
    }
  }

  async function createSharedExpense(event) {
    event.preventDefault();
    try {
      const shares = sharedForm.shares.split(",").filter(Boolean).map((entry) => {
        const [userEmail, amount] = entry.split(":").map((value) => value.trim());
        return { userEmail, amount: Number(amount) };
      });
      await request(`/groups/${selectedGroupId}/expenses`, { method: "POST", body: JSON.stringify({ ...sharedForm, totalAmount: Number(sharedForm.totalAmount), shares }) }, token);
      setSharedForm({ ...sharedForm, description: "", totalAmount: "", shares: "" });
      await loadGroupData(selectedGroupId);
    } catch (err) {
      setSharingError(err.message || "Could not create shared expense.");
    }
  }

  async function settle(event) {
    event.preventDefault();
    try {
      await request(`/groups/${selectedGroupId}/settlements`, { method: "POST", body: JSON.stringify({ ...settlement, amount: Number(settlement.amount) }) }, token);
      setSettlement({ toUserEmail: "", amount: "" });
      await loadGroupData(selectedGroupId);
    } catch (err) {
      setSharingError(err.message || "Could not record settlement.");
    }
  }

  const selectedGroup = groups.find((group) => String(group.id) === String(selectedGroupId));

  return (
    <section className="panel shared-panel">
      <div className="panel-heading">
        <div>
          <span className="eyebrow">SPLITWISE-LITE</span>
          <h2>Shared expenses</h2>
        </div>
        <span>{balances.length ? `${balances.length} open balance${balances.length === 1 ? "" : "s"}` : "Settled up"}</span>
      </div>
      {sharingError && <div className="form-message">{sharingError}<button onClick={() => setSharingError("")}><X size={14} /></button></div>}
      <form className="form-row" onSubmit={createGroup}>
        <label>New group<input required value={groupName} onChange={(e) => setGroupName(e.target.value)} placeholder="Roommates" /></label>
        <button className="secondary-button" type="submit">Create group</button>
      </form>
      {groups.length ? (
        <>
          <label>Group<select value={selectedGroupId} onChange={(e) => setSelectedGroupId(e.target.value)}>{groups.map((group) => <option key={group.id} value={group.id}>{group.name}</option>)}</select></label>
          <p>{selectedGroup?.members.map((member) => member.name).join(", ")}</p>
          <form className="form-row" onSubmit={addMember}>
            <label>Add member by email<input required type="email" value={memberEmail} onChange={(e) => setMemberEmail(e.target.value)} placeholder="friend@example.com" /></label>
            <button className="secondary-button" type="submit">Add member</button>
          </form>
          <div className="summary-grid">
            {balances.length ? balances.map((balance) => <article className="summary-card" key={`${balance.debtorEmail}-${balance.creditorEmail}`}><strong>{balance.debtorName} owes {balance.creditorName}</strong><small>{money(balance.amount)}</small></article>) : <article className="summary-card accent"><strong>Everyone is settled</strong><small>No outstanding balance in this group.</small></article>}
          </div>
          <form onSubmit={createSharedExpense}>
            <h3>Record a shared expense</h3>
            <div className="form-row"><label>Description<input required value={sharedForm.description} onChange={(e) => setSharedForm({ ...sharedForm, description: e.target.value })} placeholder="Groceries" /></label><label>Total<input required type="number" min="0.01" step="0.01" value={sharedForm.totalAmount} onChange={(e) => setSharedForm({ ...sharedForm, totalAmount: e.target.value })} /></label></div>
            <div className="form-row"><label>Paid by<input required type="email" value={sharedForm.payerEmail} onChange={(e) => setSharedForm({ ...sharedForm, payerEmail: e.target.value })} placeholder="payer@example.com" /></label><label>Shares<input required value={sharedForm.shares} onChange={(e) => setSharedForm({ ...sharedForm, shares: e.target.value })} placeholder="a@example.com:10,b@example.com:10" /></label></div>
            <button className="primary-button" type="submit">Add shared expense</button>
          </form>
          <form className="form-row" onSubmit={settle}>
            <label>Settle with<input required type="email" value={settlement.toUserEmail} onChange={(e) => setSettlement({ ...settlement, toUserEmail: e.target.value })} placeholder="creditor@example.com" /></label><label>Amount<input required type="number" min="0.01" step="0.01" value={settlement.amount} onChange={(e) => setSettlement({ ...settlement, amount: e.target.value })} /></label><button className="secondary-button" type="submit">Record payment</button>
          </form>
          <small>{sharedExpenses.length} shared expense{sharedExpenses.length === 1 ? "" : "s"} recorded</small>
        </>
      ) : <div className="empty-state">Create a group to start splitting expenses.</div>}
    </section>
  );
}

function App() {
  const [token, setToken] = useState(localStorage.getItem("ledgerly-token"));
  const [expenses, setExpenses] = useState([]);
  const [summary, setSummary] = useState({
    totalExpense: 0,
    totalTransactions: 0,
  });
  const [monthly, setMonthly] = useState([]);
  const [query, setQuery] = useState("");
  const [category, setCategory] = useState("All");
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [receipt, setReceipt] = useState(null);
  const [ocrResult, setOcrResult] = useState(null);
  const [ocrBusy, setOcrBusy] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  async function loadData() {
    setLoading(true);
    setError("");
    try {
      const [all, totals, trend] = await Promise.all([
        request("/expenses", {}, token),
        request("/expenses/summary", {}, token),
        request("/expenses/monthly", {}, token),
      ]);
      setExpenses(all);
      setSummary(totals);
      setMonthly(trend || []);
    } catch (err) {
      setError(
        "Could not load your ledger. Check that the API URL is reachable.",
      );
      if (err.message.includes("401") || err.message.includes("403")) logout();
    } finally {
      setLoading(false);
    }
  }
  useEffect(() => {
    if (token) loadData();
  }, [token]);
  function logout() {
    localStorage.removeItem("ledgerly-token");
    setToken(null);
  }
  async function analyzeReceipt(file) {
    if (!file) return;
    setReceipt(file);
    setOcrResult(null);
    setOcrBusy(true);
    try {
      const body = new FormData();
      body.append("file", file);
      const result = await request("/expenses/receipt/ocr", { method: "POST", body }, token);
      setOcrResult(result);
      setForm((current) => ({
        ...current,
        title: result.merchant || current.title,
        amount: result.amount ? String(result.amount) : current.amount,
        description: result.rawText || current.description,
      }));
    } catch (err) {
      setError(err.message || "Could not read this receipt.");
    } finally {
      setOcrBusy(false);
    }
  }
  async function saveExpense(event) {
    event.preventDefault();
    try {
      const expense = await request(
        "/expenses",
        {
          method: "POST",
          body: JSON.stringify({ ...form, amount: Number(form.amount) }),
        },
        token,
      );
      if (receipt) {
        const body = new FormData();
        body.append("file", receipt);
        const result = await request(
          `/expenses/${expense.id}/receipt`,
          { method: "POST", body },
          token,
        );
        setOcrResult(result);
      }
      setModal(false);
      setForm(emptyForm);
      setReceipt(null);
      setOcrResult(null);
      loadData();
    } catch {
      setError("Could not save this expense or analyze the receipt.");
    }
  }
  async function removeExpense(id) {
    if (!window.confirm("Delete this expense?")) return;
    try {
      await request(`/expenses/${id}`, { method: "DELETE" }, token);
      loadData();
    } catch {
      setError("Could not delete this expense.");
    }
  }
  async function downloadReport() {
    const response = await fetch(`${API_URL}/expenses/report/pdf`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = "Expense_Report.pdf";
    link.click();
    URL.revokeObjectURL(url);
  }
  const visibleExpenses = useMemo(
    () =>
      expenses.filter(
        (item) =>
          (category === "All" || item.category === category) &&
          `${item.title} ${item.description} ${item.category}`
            .toLowerCase()
            .includes(query.toLowerCase()),
      ),
    [expenses, category, query],
  );
  const maxMonth = Math.max(
    ...monthly.map((item) => Number(item.totalAmount || 0)),
    1,
  );

  if (!token) return <AuthScreen onAuthenticated={setToken} />;
  return (
    <div className="app-shell">
      <aside>
        <div className="brand-mark">
          <Wallet size={18} /> ledgerly
        </div>
        <nav>
          <a className="active">
            <BarChart3 size={17} /> Overview
          </a>
        </nav>
        <div className="sidebar-bottom">
          <div className="profile-dot">
            {(localStorage.getItem("ledgerly-token") || "U")
              .slice(0, 1)
              .toUpperCase()}
          </div>
          <button className="icon-button" title="Sign out" onClick={logout}>
            <LogOut size={17} />
          </button>
        </div>
      </aside>
      <main className="dashboard">
        <header>
          <div>
            <span className="eyebrow">
              {new Date()
                .toLocaleDateString("en-US", {
                  weekday: "long",
                  month: "long",
                  day: "numeric",
                })
                .toUpperCase()}
            </span>
            <h1>Good to see you.</h1>
          </div>
          <div className="header-actions">
            <button className="secondary-button" onClick={downloadReport}>
              <Download size={16} /> Export PDF
            </button>
            <button
              className="primary-button compact"
              onClick={() => setModal(true)}
            >
              <Plus size={17} /> Add expense
            </button>
          </div>
        </header>
        {error && (
          <div className="notice">
            {error}
            <button onClick={() => setError("")}>
              <X size={15} />
            </button>
          </div>
        )}
        <section className="summary-grid">
          <article className="summary-card accent">
            <div className="card-label">
              TOTAL SPEND <ArrowDownRight size={15} />
            </div>
            <strong>{money(summary.totalExpense)}</strong>
            <small>Across your recorded expenses</small>
          </article>
          <article className="summary-card">
            <div className="card-label">
              TRANSACTIONS <ArrowUpRight size={15} />
            </div>
            <strong>{summary.totalTransactions}</strong>
            <small>Items in your ledger</small>
          </article>
          <article className="summary-card">
            <div className="card-label">AVERAGE SPEND</div>
            <strong>
              {money(
                summary.totalTransactions
                  ? summary.totalExpense / summary.totalTransactions
                  : 0,
              )}
            </strong>
            <small>Per transaction</small>
          </article>
        </section>
        <section className="content-grid">
          <article className="panel trend-panel">
            <div className="panel-heading">
              <div>
                <span className="eyebrow">MOMENTUM</span>
                <h2>Monthly spending</h2>
              </div>
              <CalendarDays size={19} />
            </div>
            <div className="chart">
              {monthly.length ? (
                monthly.map((item) => (
                  <div className="bar-wrap" key={item.month}>
                    <span>{money(item.totalAmount)}</span>
                    <div
                      className="bar"
                      style={{
                        height: `${Math.max(8, (Number(item.totalAmount) / maxMonth) * 100)}%`,
                      }}
                    />
                    <small>{item.month}</small>
                  </div>
                ))
              ) : (
                <div className="empty-state">
                  Your monthly rhythm will appear here.
                </div>
              )}
            </div>
          </article>
          <article className="panel insight-panel">
            <span className="eyebrow">LEDGER NOTE</span>
            <h2>
              {summary.totalTransactions
                ? "Small entries add up."
                : "Start with one entry."}
            </h2>
            <p>
              {summary.totalTransactions
                ? "Keep logging the ordinary purchases. Patterns become useful when they become visible."
                : "Add your first expense to start seeing your spending in context."}
            </p>
            <div className="insight-rule" />
          </article>
        </section>
        <SharedExpensesPanel token={token} />
        <section className="panel expenses-panel">
          <div className="panel-heading">
            <div>
              <span className="eyebrow">RECENT ACTIVITY</span>
              <h2>Your expenses</h2>
            </div>
            <div className="filters">
              <div className="search">
                <Search size={16} />
                <input
                  value={query}
                  onChange={(e) => setQuery(e.target.value)}
                  placeholder="Search ledger"
                />
              </div>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
              >
                <option>All</option>
                {categories.map((item) => (
                  <option key={item}>{item}</option>
                ))}
              </select>
            </div>
          </div>
          {loading ? (
            <div className="empty-state">Loading your ledger...</div>
          ) : visibleExpenses.length ? (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>DETAIL</th>
                    <th>CATEGORY</th>
                    <th>DATE</th>
                    <th>AMOUNT</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {visibleExpenses.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <strong>{item.title}</strong>
                        <small>{item.description}</small>
                      </td>
                      <td>
                        <span className="category-pill">{item.category}</span>
                      </td>
                      <td>{item.date}</td>
                      <td className="amount">{money(item.amount)}</td>
                      <td>
                        <button
                          className="delete-button"
                          title="Delete expense"
                          onClick={() => removeExpense(item.id)}
                        >
                          <Trash2 size={15} />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="empty-state">No expenses match your filters.</div>
          )}
        </section>
      </main>
      {modal && (
        <div
          className="modal-backdrop"
          onMouseDown={(e) => e.target === e.currentTarget && setModal(false)}
        >
          <form className="modal" onSubmit={saveExpense}>
            <div className="modal-heading">
              <div>
                <span className="eyebrow">NEW ENTRY</span>
                <h2>Add an expense</h2>
              </div>
              <button
                type="button"
                className="icon-button"
                onClick={() => setModal(false)}
              >
                <X size={18} />
              </button>
            </div>
            <label>
              What was it?
              <input
                required
                value={form.title}
                onChange={(e) => setForm({ ...form, title: e.target.value })}
                placeholder="Coffee with a friend"
              />
            </label>
            <div className="form-row">
              <label>
                Amount
                <input
                  required
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={form.amount}
                  onChange={(e) => setForm({ ...form, amount: e.target.value })}
                  placeholder="0.00"
                />
              </label>
              <label>
                Category
                <select
                  value={form.category}
                  onChange={(e) =>
                    setForm({ ...form, category: e.target.value })
                  }
                >
                  {categories.map((item) => (
                    <option key={item}>{item}</option>
                  ))}
                </select>
              </label>
            </div>
            <div className="form-row">
              <label>
                Date
                <input
                  required
                  type="date"
                  value={form.date}
                  onChange={(e) => setForm({ ...form, date: e.target.value })}
                />
              </label>
              <label>
                Type
                <select
                  value={form.type}
                  onChange={(e) => setForm({ ...form, type: e.target.value })}
                >
                  <option>Expense</option>
                  <option>Income</option>
                </select>
              </label>
            </div>
            <label>
              Description
              <textarea
                required
                rows="3"
                value={form.description}
                onChange={(e) =>
                  setForm({ ...form, description: e.target.value })
                }
                placeholder="A little context helps later."
              />
            </label>
            <label>
              Receipt photo
              <input
                type="file"
                accept="image/*"
                onChange={(e) => analyzeReceipt(e.target.files?.[0])}
              />
            </label>
            {ocrBusy && <div className="form-message">Reading receipt...</div>}
            {ocrResult && !ocrBusy && (
              <div className="form-message">
                {ocrResult.message || "Receipt analyzed. Review the fields before saving."}
              </div>
            )}
            <button className="primary-button" type="submit">
              <Plus size={17} /> {ocrBusy ? "Reading receipt..." : "Save expense"}
            </button>
          </form>
        </div>
      )}
    </div>
  );
}

export default App;

createRoot(document.getElementById("root")).render(<App />);
