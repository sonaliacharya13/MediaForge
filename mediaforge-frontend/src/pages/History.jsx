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

function History() {
  const navigate = useNavigate();

  const [media, setMedia] = useState([]);
  const [loading, setLoading] = useState(true);

  const [filename, setFilename] = useState("");
  const [format, setFormat] = useState("");
  const [status, setStatus] = useState("");

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const username = getUsernameFromToken();

  const pageSize = 10;

  useEffect(() => {
    fetchMedia();
  }, [page]);

  const fetchMedia = async () => {
    try {
      setLoading(true);
      setError("");

      const hasFilters =
        filename.trim() !== "" ||
        format !== "" ||
        status !== "";

      let response;

      if (hasFilters) {
        response = await api.get("/media/search", {
          params: {
            filename: filename.trim() || undefined,
            format: format || undefined,
            status: status || undefined,
            page,
            size: pageSize,
          },
        });
      } else {
        response = await api.get("/media/history", {
          params: {
            page,
            size: pageSize,
          },
        });
      }

      setMedia(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);

    } catch (err) {
      console.error("Failed to fetch history:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Failed to load media history."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();

    setPage(0);

    // Fetch immediately because page is currently reset manually.
    fetchMediaForPage(0);
  };

  const fetchMediaForPage = async (requestedPage) => {
    try {
      setLoading(true);
      setError("");

      const hasFilters =
        filename.trim() !== "" ||
        format !== "" ||
        status !== "";

      let response;

      if (hasFilters) {
        response = await api.get("/media/search", {
          params: {
            filename: filename.trim() || undefined,
            format: format || undefined,
            status: status || undefined,
            page: requestedPage,
            size: pageSize,
          },
        });
      } else {
        response = await api.get("/media/history", {
          params: {
            page: requestedPage,
            size: pageSize,
          },
        });
      }

      setMedia(response.data.content || []);
      setTotalPages(response.data.totalPages || 0);
      setTotalElements(response.data.totalElements || 0);
      setPage(requestedPage);

    } catch (err) {
      console.error("Search error:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Failed to search media."
      );
    } finally {
      setLoading(false);
    }
  };

  const handleClearFilters = () => {
    setFilename("");
    setFormat("");
    setStatus("");
    setPage(0);
    setMessage("");

    fetchMediaForPage(0);
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

      setMessage("Media deleted successfully.");

      fetchMediaForPage(page);

    } catch (err) {
      console.error("Delete error:", err);

      setError(
        err.response?.data?.message ||
        err.response?.data ||
        "Failed to delete media."
      );
    }
  };

  const handleDownload = async (id, fileName) => {
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

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");

      link.href = url;
      link.download = fileName || "optimized-media";

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

  const formatSize = (bytes) => {
    if (!bytes || bytes === 0) {
      return "0 B";
    }

    const units = ["B", "KB", "MB", "GB"];

    const index = Math.floor(
      Math.log(bytes) / Math.log(1024)
    );

    return `${(
      bytes / Math.pow(1024, index)
    ).toFixed(index === 0 ? 0 : 2)} ${
      units[index] || "GB"
    }`;
  };

  const getCompression = (item) => {
    if (
      !item.originalSize ||
      !item.optimizedSize ||
      item.optimizedSize >= item.originalSize
    ) {
      return "0%";
    }

    const percentage =
      ((item.originalSize - item.optimizedSize) /
        item.originalSize) *
      100;

    return `${percentage.toFixed(1)}%`;
  };

  const getStatusClass = (itemStatus) => {
    switch (itemStatus) {
      case "COMPLETED":
        return "status-completed";

      case "PROCESSING":
        return "status-processing";

      case "PENDING":
        return "status-pending";

      default:
        return "status-default";
    }
  };

  const handleLogout = () => {
    localStorage.removeItem("token");
    navigate("/");
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

          <button
            type="button"
            className="nav-item"
            onClick={() => navigate("/dashboard")}
          >
            <span>⌂</span>
            Dashboard
          </button>

          <button
            type="button"
            className="nav-item"
            onClick={() => navigate("/dashboard#upload")}
          >
            <span>↑</span>
            Upload Media
          </button>

          <button
            type="button"
            className="nav-item active"
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
            <h2>Media History</h2>

            <p>
              View and manage your processed media.
            </p>
          </div>

          <div className="user-area">

            <div className="user-avatar">
              {username.charAt(0).toUpperCase()}
            </div>

            <span>
              {username}
            </span>

          </div>

        </header>

        {/* CONTENT */}

        <main className="dashboard-content">

          <section className="welcome">

            <div>

              <h1>
                Media History
              </h1>

              <p>
                Search, filter and manage your uploaded files.
              </p>

            </div>

          </section>

          {/* FILTERS */}

          <section className="history-filters">

            <form
              className="filter-form"
              onSubmit={handleSearch}
            >

              <input
                type="text"
                placeholder="Search filename..."
                value={filename}
                onChange={(e) =>
                  setFilename(e.target.value)
                }
              />

              <select
                value={format}
                onChange={(e) => {
                  setFormat(e.target.value);
                  setPage(0);
                }}
              >
                <option value="">
                  All Formats
                </option>

                <option value="jpg">
                  JPG
                </option>

                <option value="jpeg">
                  JPEG
                </option>

                <option value="png">
                  PNG
                </option>

                <option value="webp">
                  WEBP
                </option>

                <option value="mp4">
                  MP4
                </option>

                <option value="mov">
                  MOV
                </option>
              </select>

              <select
                value={status}
                onChange={(e) => {
                  setStatus(e.target.value);
                  setPage(0);
                }}
              >
                <option value="">
                  All Status
                </option>

                <option value="COMPLETED">
                  Completed
                </option>

                <option value="PROCESSING">
                  Processing
                </option>

                <option value="PENDING">
                  Pending
                </option>
              </select>

              <button
                type="submit"
                className="primary-btn"
              >
                Search
              </button>

              <button
                type="button"
                className="secondary-btn"
                onClick={handleClearFilters}
              >
                Clear
              </button>

            </form>

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

          {/* HISTORY */}

          <section className="history-card">

            <div className="section-heading">

              <div>

                <h2>
                  Your Media
                </h2>

                <p>
                  {totalElements} media file
                  {totalElements !== 1 ? "s" : ""}
                </p>

              </div>

            </div>

            {loading ? (

              <div className="empty-state">

                <div className="empty-icon">
                  ◷
                </div>

                <h3>
                  Loading...
                </h3>

                <p>
                  Fetching your media history.
                </p>

              </div>

            ) : media.length === 0 ? (

              <div className="empty-state">

                <div className="empty-icon">
                  ◷
                </div>

                <h3>
                  No media found
                </h3>

                <p>
                  Try changing your search or filters.
                </p>

              </div>

            ) : (

              <div className="media-list">

                {media.map((item) => (

                  <div
                    className="media-item"
                    key={item.id}
                  >

                    <div className="media-info">

                      <strong>
                        {item.filename}
                      </strong>

                      <span>
                        {item.format || "Media"}
                        {" • "}
                        {formatSize(item.originalSize)}
                        {" → "}
                        {formatSize(item.optimizedSize)}
                      </span>

                    </div>

                    <div className="media-status">

                      <span
                        className={getStatusClass(
                          item.status
                        )}
                      >
                        {item.status}
                      </span>

                      {item.status === "COMPLETED" && (
                        <small>
                          Saved {getCompression(item)}
                        </small>
                      )}

                    </div>

                    <div className="media-actions">

                      {item.status === "COMPLETED" && (
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
                          handleDelete(item.id)
                        }
                      >
                        Delete
                      </button>

                    </div>

                  </div>

                ))}

              </div>

            )}

            {/* PAGINATION */}

            {!loading && totalPages > 1 && (

              <div className="pagination">

                <button
                  type="button"
                  className="secondary-btn"
                  disabled={page === 0}
                  onClick={() =>
                    fetchMediaForPage(page - 1)
                  }
                >
                  Previous
                </button>

                <span>
                  Page {page + 1} of {totalPages}
                </span>

                <button
                  type="button"
                  className="secondary-btn"
                  disabled={page >= totalPages - 1}
                  onClick={() =>
                    fetchMediaForPage(page + 1)
                  }
                >
                  Next
                </button>

              </div>

            )}

          </section>

        </main>

      </div>

    </div>
  );
}

export default History;