import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import api from "../services/api";

function getUsernameFromToken() {
  const token = localStorage.getItem("token");

  if (!token) {
    return "User";
  }

  try {
    const payload = JSON.parse(atob(token.split(".")[1]));
    return payload.sub || "User";
  } catch (error) {
    console.error("Invalid JWT token:", error);
    return "User";
  }
}

function Dashboard() {
  const navigate = useNavigate();

  const [file, setFile] = useState(null);
  const [media, setMedia] = useState([]);

  const [uploading, setUploading] = useState(false);
  const [optimizing, setOptimizing] = useState(false);
  const [loadingMedia, setLoadingMedia] = useState(true);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const username = getUsernameFromToken();

  useEffect(() => {
    fetchMedia();
  }, []);

  const fetchMedia = async () => {
    try {
      setLoadingMedia(true);

      const response = await api.get("/media");

      setMedia(response.data);
    } catch (err) {
      console.error("Failed to fetch media:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Failed to load media."
      );
    } finally {
      setLoadingMedia(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
  };

  const handleFileChange = (e) => {
    const selectedFile = e.target.files[0];

    setMessage("");
    setError("");

    if (!selectedFile) {
      return;
    }

    setFile(selectedFile);
  };

  const handleUpload = async (e) => {
    e.preventDefault();

    if (!file) {
      setError("Please select a media file first.");
      return;
    }

    setUploading(true);
    setOptimizing(false);
    setMessage("");
    setError("");

    const formData = new FormData();
    formData.append("file", file);

    try {
      // 1. Upload media
      const uploadResponse = await api.post(
        "/media/upload",
        formData
      );

      const mediaId = uploadResponse.data.id;

      if (!mediaId) {
        throw new Error(
          "Media ID was not returned by the server."
        );
      }

      setMessage(
        "Media uploaded. Starting optimization..."
      );

      // 2. Optimize media
      setOptimizing(true);

      await api.post(
        `/media/${mediaId}/optimize`
      );

      setMessage(
        "Optimization completed successfully."
      );

      setFile(null);

      // 3. Refresh media list
      await fetchMedia();

    } catch (err) {
      console.error(
        "Upload/optimization error:",
        err
      );

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        err.message ||
        "Upload or optimization failed."
      );
    } finally {
      setUploading(false);
      setOptimizing(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this media?"
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setMessage("");

      await api.delete(`/media/${id}`);

      setMessage(
        "Media deleted successfully."
      );

      await fetchMedia();

    } catch (err) {
      console.error("Delete error:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Failed to delete media."
      );
    }
  };

  const handleDownload = async (id, filename) => {
    try {
      setError("");

      const response = await api.get(
        `/media/${id}/download`,
        {
          responseType: "blob",
        }
      );

      const blob = new Blob(
        [response.data],
        {
          type:
            response.headers["content-type"] ||
            "application/octet-stream",
        }
      );

      const url =
        window.URL.createObjectURL(blob);

      const link =
        document.createElement("a");

      link.href = url;
      link.download =
        filename || "optimized-media";

      document.body.appendChild(link);

      link.click();

      link.remove();

      window.URL.revokeObjectURL(url);

    } catch (err) {
      console.error("Download error:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Failed to download media."
      );
    }
  };

  const totalMedia = media.length;

  const optimizedMedia = media.filter(
    (item) => item.status === "COMPLETED"
  ).length;

  const processingMedia = media.filter(
    (item) =>
      item.status === "PROCESSING" ||
      item.status === "PENDING"
  ).length;

  const storageSaved = media.reduce(
    (total, item) => {
      if (
        item.originalSize &&
        item.optimizedSize &&
        item.optimizedSize < item.originalSize
      ) {
        return (
          total +
          (item.originalSize -
            item.optimizedSize)
        );
      }

      return total;
    },
    0
  );

  const storageSavedMB = (
    storageSaved /
    (1024 * 1024)
  ).toFixed(2);

  const formatSize = (bytes) => {
    if (!bytes || bytes === 0) {
      return "0 B";
    }

    const units = [
      "B",
      "KB",
      "MB",
      "GB",
    ];

    const index = Math.floor(
      Math.log(bytes) /
        Math.log(1024)
    );

    return `${(
      bytes /
      Math.pow(1024, index)
    ).toFixed(index === 0 ? 0 : 2)} ${
      units[index]
    }`;
  };

  const getCompression = (item) => {
    if (
      !item.originalSize ||
      !item.optimizedSize ||
      item.optimizedSize >=
        item.originalSize
    ) {
      return "0%";
    }

    const percentage =
      ((item.originalSize -
        item.optimizedSize) /
        item.originalSize) *
      100;

    return `${percentage.toFixed(1)}%`;
  };

  return (
    <div className="app-layout">

      {/* SIDEBAR */}

      <aside className="sidebar">

        <div className="brand">

          <div className="brand-mark">
            M
          </div>

          <div>
            <h1>MediaForge</h1>

            <span>
              Media Platform
            </span>
          </div>

        </div>

        <nav className="sidebar-nav">

          <a
            href="#dashboard"
            className="nav-item active"
          >
            <span>⌂</span>
            Dashboard
          </a>

          <a
            href="#upload"
            className="nav-item"
          >
            <span>↑</span>
            Upload Media
          </a>

          <button
  type="button"
  className="nav-item"
  onClick={() => navigate("/history")}
>
  <span>◷</span>
  History
</button>

        </nav>

        <div className="sidebar-bottom">

          <button
            type="button"
            className="logout-btn"
            onClick={handleLogout}
          >
            <span>↪</span>
            Logout
          </button>

        </div>

      </aside>

      {/* MAIN AREA */}

      <div className="main-area">

        {/* TOPBAR */}

        <header className="topbar">

          <div>
            <h2>Dashboard</h2>

            <p>
              Manage and optimize your
              media files.
            </p>
          </div>

          {/* ACTUAL LOGGED-IN USER */}

          <div className="user-area">

            <div className="user-avatar">
              {username
                .charAt(0)
                .toUpperCase()}
            </div>

            <span>
              {username}
            </span>

          </div>

        </header>

        <main
          className="dashboard-content"
          id="dashboard"
        >

          {/* WELCOME */}

          <section className="welcome">

            <div>

              <h1>
                Welcome back, {username}
              </h1>

              <p>
                Upload your media and let
                MediaForge handle the
                optimization.
              </p>

            </div>

          </section>

          {/* STATISTICS */}

          <section className="stats-grid">

            <div className="stat-card">

              <div className="stat-top">

                <span>
                  Total Media
                </span>

                <div className="stat-icon">
                  ◈
                </div>

              </div>

              <strong>
                {totalMedia}
              </strong>

              <p>
                Your uploaded files
              </p>

            </div>

            <div className="stat-card">

              <div className="stat-top">

                <span>
                  Optimized
                </span>

                <div className="stat-icon">
                  ✓
                </div>

              </div>

              <strong>
                {optimizedMedia}
              </strong>

              <p>
                Successfully processed
              </p>

            </div>

            <div className="stat-card">

              <div className="stat-top">

                <span>
                  Processing
                </span>

                <div className="stat-icon">
                  ◷
                </div>

              </div>

              <strong>
                {processingMedia}
              </strong>

              <p>
                Currently processing
              </p>

            </div>

            <div className="stat-card">

              <div className="stat-top">

                <span>
                  Storage Saved
                </span>

                <div className="stat-icon">
                  ↓
                </div>

              </div>

              <strong>
                {storageSavedMB} MB
              </strong>

              <p>
                Total optimization savings
              </p>

            </div>

          </section>

          {/* MESSAGES */}

          {message && (
            <div className="success">
              {message}
            </div>
          )}

          {error && (
            <div className="error">
              {error}
            </div>
          )}

          {/* WORKSPACE */}

          <section className="workspace">

            {/* UPLOAD */}

            <div
              className="upload-card"
              id="upload"
            >

              <div className="section-heading">

                <div>

                  <h2>
                    Optimize Media
                  </h2>

                  <p>
                    Select an image or video
                    to upload.
                  </p>

                </div>

              </div>

              <div className="drop-zone">

                <div className="upload-icon">
                  ↑
                </div>

                <h3>
                  {file
                    ? file.name
                    : "Select your media file"}
                </h3>

                <p>
                  JPG, PNG, WEBP, MP4, MOV
                </p>

                <input
                  id="media-file"
                  type="file"
                  accept="image/*,video/*"
                  onChange={
                    handleFileChange
                  }
                  hidden
                />

                <label
                  htmlFor="media-file"
                  className="upload-btn"
                >
                  Select Media
                </label>

                {file && (
                  <button
                    type="button"
                    className="primary-btn"
                    onClick={handleUpload}
                    disabled={uploading}
                    style={{
                      marginTop: "10px",
                    }}
                  >
                    {uploading
                      ? optimizing
                        ? "Optimizing..."
                        : "Uploading..."
                      : "Upload Media"}
                  </button>
                )}

              </div>

            </div>

            {/* RECENT ACTIVITY */}

            <div
              className="activity-card"
              id="history"
            >

              <div className="section-heading">

                <div>

                  <h2>
                    Recent Activity
                  </h2>

                  <p>
                    Your latest media
                    processing activity.
                  </p>

                </div>
                <button
  type="button"
  className="text-btn"
  onClick={() => navigate("/history")}
>
  View all
</button>

              </div>

              {loadingMedia ? (

                <div className="empty-state">

                  <div className="empty-icon">
                    ◷
                  </div>

                  <h3>
                    Loading...
                  </h3>

                  <p>
                    Fetching your media.
                  </p>

                </div>

              ) : media.length === 0 ? (

                <div className="empty-state">

                  <div className="empty-icon">
                    ◷
                  </div>

                  <h3>
                    No activity yet
                  </h3>

                  <p>
                    Your uploaded media
                    will appear here.
                  </p>

                </div>

              ) : (

                <div className="media-list">

                  {media
                    .slice(0, 5)
                    .map((item) => (

                      <div
                        className="media-item"
                        key={item.id}
                      >

                        <div className="media-info">

                          <strong>
                            {item.filename}
                          </strong>

                          <span>
                            {item.format ||
                              "Media"}{" "}
                            •{" "}
                            {formatSize(
                              item.originalSize
                            )}
                          </span>

                        </div>

                        <div className="media-status">

                          <span>
                            {item.status}
                          </span>

                          {item.status ===
                            "COMPLETED" && (
                            <small>
                              Saved{" "}
                              {getCompression(
                                item
                              )}
                            </small>
                          )}

                        </div>

                        <div className="media-actions">

                          {item.status ===
                            "COMPLETED" && (
                            <button
                              type="button"
                              className="text-btn"
                              onClick={() =>
                                handleDownload(
                                  item.id,
                                  item.filename
                                )
                              }
                            >
                              Download
                            </button>
                          )}

                          <button
                            type="button"
                            className="text-btn"
                            onClick={() =>
                              handleDelete(
                                item.id
                              )
                            }
                          >
                            Delete
                          </button>

                        </div>

                      </div>

                    ))}

                </div>

              )}

            </div>

          </section>

        </main>

      </div>

    </div>
  );
}

export default Dashboard;