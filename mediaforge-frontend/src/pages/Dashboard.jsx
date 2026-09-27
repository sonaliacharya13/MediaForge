import { useNavigate } from "react-router-dom";

function Dashboard() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  return (
    <div className="dashboard">
      <h1>MediaForge Dashboard</h1>
      <p>Welcome to your media optimization workspace.</p>

      <button onClick={handleLogout}>Logout</button>
    </div>
  );
}

export default Dashboard;