import { useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function Dashboard() {
  const navigate = useNavigate();

  const [file, setFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];

    setMessage("");
    setError("");

    if (!selectedFile) return;

    setFile(selectedFile);
  };

  const handleUpload = async () => {
    if (!file) {
      setError("Please select a media file first.");
      return;
    }

    setUploading(true);
    setMessage("");
    setError("");

    const formData = new FormData();
    formData.append("file", file);

    try {
      await api.post("/media/upload", formData);

      setMessage("Media uploaded successfully.");
      setFile(null);
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Upload failed."
      );
    } finally {
      setUploading(false);
    }
  };

  return (
    <div className="app-layout">

      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">M</div>
          <div>
            <h1>MediaForge</h1>
            <span>Media Platform</span>
          </div>
        </div>

        <nav className="sidebar-nav">
          <a className="nav-item active">
            <span>⌂</span>
            Dashboard
          </a>

          <a className="nav-item">
            <span>↑</span>
            Upload Media
          </a>

          <a className="nav-item">
            <span>◷</span>
            History
          </a>
        </nav>

        <div className="sidebar-bottom">
          <button className="logout-btn" onClick={handleLogout}>
            <span>↪</span>
            Logout
          </button>
        </div>
      </aside>

      <div className="main-area">

        <header className="topbar">
          <div>
            <h2>Dashboard</h2>
            <p>Manage and optimize your media files.</p>
          </div>

          <div className="user-area">
            <div className="user-avatar">U</div>
            <span>User</span>
          </div>
        </header>

        <main className="dashboard-content">

          <section className="welcome">
            <div>
              <h1>Welcome back</h1>
              <p>
                Upload your media and let MediaForge handle the optimization.
              </p>
            </div>
          </section>

          <section className="stats-grid">
            <div className="stat-card">
              <div className="stat-top">
                <span>Total Media</span>
                <div className="stat-icon">◈</div>
              </div>
              <strong>0</strong>
              <p>Your uploaded files</p>
            </div>

            <div className="stat-card">
              <div className="stat-top">
                <span>Optimized</span>
                <div className="stat-icon">✓</div>
              </div>
              <strong>0</strong>
              <p>Successfully processed</p>
            </div>

            <div className="stat-card">
              <div className="stat-top">
                <span>Processing</span>
                <div className="stat-icon">◷</div>
              </div>
              <strong>0</strong>
              <p>Currently processing</p>
            </div>

            <div className="stat-card">
              <div className="stat-top">
                <span>Storage Saved</span>
                <div className="stat-icon">↓</div>
              </div>
              <strong>0 MB</strong>
              <p>Total optimization savings</p>
            </div>
          </section>

          <section className="workspace">

            <div className="upload-card">
              <div className="section-heading">
                <div>
                  <h2>Optimize Media</h2>
                  <p>Select an image or video to upload.</p>
                </div>
              </div>

              <div className="drop-zone">

                <div className="upload-icon">↑</div>

                <h3>
                  {file ? file.name : "Select your media file"}
                </h3>

                <p>
                  JPG, PNG, WEBP, MP4, MOV
                </p>

                <input
                  id="media-file"
                  type="file"
                  accept="image/*,video/*"
                  onChange={handleFileChange}
                  hidden
                />

                <label htmlFor="media-file" className="upload-btn">
                  Select Media
                </label>

                {file && (
                  <button
                    className="primary-btn"
                    onClick={handleUpload}
                    disabled={uploading}
                    style={{ marginTop: "10px" }}
                  >
                    {uploading ? "Uploading..." : "Upload Media"}
                  </button>
                )}

                {message && (
                  <p className="success">
                    {message}
                  </p>
                )}

                {error && (
                  <p className="error">
                    {error}
                  </p>
                )}

              </div>
            </div>

            <div className="activity-card">
              <div className="section-heading">
                <div>
                  <h2>Recent Activity</h2>
                  <p>Your latest media processing activity.</p>
                </div>
              </div>

              <div className="empty-state">
                <div className="empty-icon">◷</div>
                <h3>No activity yet</h3>
                <p>Your uploaded media will appear here.</p>
              </div>
            </div>

          </section>

        </main>
      </div>
    </div>
  );
}

export default Dashboard;