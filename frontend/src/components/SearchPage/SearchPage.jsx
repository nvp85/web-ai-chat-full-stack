import { useState } from "react";
import { searchMessages } from "../../api/api";
import "./SearchPage.css";
import { NavLink } from "react-router";
import { useAuth } from "../../hooks/useAuth";
import ErrorModal from "../Modal/ErrorModal";
import { FaSpinner } from "react-icons/fa";


export default function SearchPage() {
    const [searchResult, setSearchResult] = useState(null);
    const [query, setQuery] = useState("");
    const [searchType, setSearchType] = useState("text");
    const { token, handleUnauthorized } = useAuth();
    const [error, setError] = useState(null);
    const [searching, setSearching] = useState(false);

    async function search(e) {
        e.preventDefault();
        setSearchResult(null);
        if (query.length < 3) {
            setError("The query must be at least 3 characters long.");
            return;
        }
        try {
            setSearching(true);
            const messages = await searchMessages(query, searchType, token);
            setSearchResult(messages);
        } catch (err) {
            if (err.message === "Invalid credentials.") {
                handleUnauthorized();
                return;
            }
            setError(err.message);
        }
        setSearching(false);
    }
    function displaySearchResults() {
        if (searchResult?.length === 0) {
            return (<p>Noting was found.</p>)
        }
        return searchResult.map(m => <div key={m.message.id} className="search-result-item">
            <p>Chat: <NavLink to={`/chats/${m.chatId}`}>{m.chatTitle}</NavLink></p>
            <p>{m.message.content}</p>
        </div>)
    }

    return (
        <div className="search-result-container">
            <form onSubmit={search} className="search-form">
                <fieldset>
                    <legend>Select a type of search:</legend>
                    <div>
                        <input
                            type="radio"
                            id="text"
                            name="searchType"
                            value="text"
                            checked={searchType === "text"}
                            onChange={(e) => setSearchType(e.target.value)}
                        />
                        <label htmlFor="text">By keyword (better for short queries)</label>
                    </div>

                    <div>
                        <input
                            type="radio"
                            id="vector"
                            name="searchType"
                            value="vector"
                            checked={searchType === "vector"}
                            onChange={(e) => setSearchType(e.target.value)}
                        />
                        <label htmlFor="vector">By meaning (better for longer queries)</label>
                    </div>
                </fieldset>
                <input
                    type="text"
                    name="query"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    className="search-input"
                />
                <button className="btn">Search</button>
            </form>
            {
                searchResult !== null && displaySearchResults()                
            }
            { searching && <p>Searching <FaSpinner className="spinner"/> </p>}
            { error && 
                <ErrorModal onClose={() => setError("")}> {error} </ErrorModal>
            }
        </div>
    )
}