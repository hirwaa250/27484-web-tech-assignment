import { useEffect, useMemo, useState } from 'react'
import axios from 'axios'
import './office.css'

function getErrorMessage(error, fallback) {
    const responseData = error.response ? error.response.data : null;
    if (typeof responseData === "string" && responseData) {
        if (/<\s*!doctype|<\s*html/i.test(responseData)) {
            const status = error.response.status;
            return "The office request failed (HTTP " + status +
                "). Check that the backend API route is available.";
        }
        return responseData;
    }
    if (responseData) {
        return JSON.stringify(responseData);
    }
    return error.message || fallback;
}

async function fetchAllOffices() {
    const response = await axios.get("/api/offices/free");
    if (!Array.isArray(response.data)) {
        throw new Error("The server returned an unexpected office list.");
    }
    return response.data;
}

function normalizeSearchValue(value) {
    return String(value ?? "").trim().toLowerCase();
}

function Office() {
    const [offices, setOffices] = useState([]);
    const [searchText, setSearchText] = useState("");
    const [searchTerm, setSearchTerm] = useState("");
    const [isLoading, setIsLoading] = useState(true);
    const [loadError, setLoadError] = useState("");
    const [actionMessage, setActionMessage] = useState("");
    const [actionError, setActionError] = useState("");
    const [deletingId, setDeletingId] = useState(null);

    async function getAllOffices(isRetry = false) {
        if (isRetry) {
            setIsLoading(true);
            setLoadError("");
        }

        try {
            setOffices(await fetchAllOffices());
            setLoadError("");
        } catch (error) {
            setLoadError(getErrorMessage(error, "Unable to load offices."));
        } finally {
            setIsLoading(false);
        }
    }

    useEffect(() => {
        let isMounted = true;

        fetchAllOffices()
            .then(officeList => {
                if (isMounted) {
                    setOffices(officeList);
                    setLoadError("");
                }
            })
            .catch(error => {
                if (isMounted) {
                    setLoadError(getErrorMessage(error, "Unable to load offices."));
                }
            })
            .finally(() => {
                if (isMounted) {
                    setIsLoading(false);
                }
            });

        return () => {
            isMounted = false;
        };
    }, []);

    const filteredOffices = useMemo(() => {
        const appliedSearchTerm = normalizeSearchValue(searchTerm);
        if (!appliedSearchTerm) {
            return offices;
        }

        return offices.filter(office =>
            normalizeSearchValue(office.name).includes(appliedSearchTerm) ||
            normalizeSearchValue(office.officeNumber).includes(appliedSearchTerm)
        );
    }, [offices, searchTerm]);

    function handleSearch(event) {
        event.preventDefault();
        setSearchTerm(searchText.trim());
    }

    async function deleteOffice(office) {
        const confirmed = window.confirm(
            "Delete " + (office.name || "this office") +
            " (Office #" + office.officeNumber + ")?"
        );
        if (!confirmed) {
            return;
        }

        setDeletingId(office.id);
        setActionMessage("");
        setActionError("");

        try {
            await axios.delete("/api/offices/delete/" + office.id);
            setOffices(currentOffices =>
                currentOffices.filter(item => item.id !== office.id)
            );
            setActionMessage("Office #" + office.officeNumber + " was deleted.");
        } catch (error) {
            setActionError(getErrorMessage(error, "Unable to delete this office."));
        } finally {
            setDeletingId(null);
        }
    }

    return (
        <main className="offices-page">
            <section className="offices-content" aria-labelledby="offices-title">
                <header className="offices-header">
                    <div>
                        <p>LIBRARY DIRECTORY</p>
                        <h1 id="offices-title">Offices Page</h1>
                    </div>

                    <form className="office-search" onSubmit={handleSearch}>
                        <label htmlFor="office-search">Search offices</label>
                        <div className="office-search-controls">
                            <input
                                id="office-search"
                                type="text"
                                value={searchText}
                                onChange={e => setSearchText(e.target.value)}
                                placeholder="Name or number"
                            />
                            <button type="submit">Search</button>
                        </div>
                    </form>
                </header>

                {actionMessage && (
                    <p role="status">{actionMessage}</p>
                )}
                {actionError && (
                    <p role="alert">{actionError}</p>
                )}

                {isLoading ? (
                    <p>Loading offices...</p>
                ) : loadError ? (
                    <div className="office-status">
                        <p role="alert">{loadError}</p>
                        <button type="button" onClick={() => getAllOffices(true)}>
                            Try again
                        </button>
                    </div>
                ) : (
                    <div className="office-table-container">
                        <table className="office-table">
                            <thead>
                                <tr>
                                    <th scope="col">ID</th>
                                    <th scope="col">Office number</th>
                                    <th scope="col">Office name</th>
                                    <th scope="col">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredOffices.map((office, index) => (
                                    <tr key={office.id}>
                                        <td>{index + 1}</td>
                                        <td>{office.officeNumber}</td>
                                        <td>{office.name}</td>
                                        <td>
                                            <button
                                                type="button"
                                                onClick={() => deleteOffice(office)}
                                                disabled={deletingId !== null}
                                            >
                                                {deletingId === office.id ? "Deleting..." : "Delete"}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>

                        {filteredOffices.length === 0 && (
                            <p className="office-status">
                                {searchTerm
                                    ? "No offices match your search."
                                    : "No available offices are currently listed."}
                            </p>
                        )}
                    </div>
                )}
            </section>
        </main>
    );
}

export default Office
