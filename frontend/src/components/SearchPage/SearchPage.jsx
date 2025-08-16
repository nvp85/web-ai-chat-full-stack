import { useState } from "react";
import { searchMessages } from "../../api/api";


export default function SearchPage() {
    const [searchResult, setSearchResult] = useState(null);
    const [query, setQuery] = useState("");

    async function search(e) {
        e.preventDefault();
        if (query.length < 3) {
            return;
        }
        const messages = await searchMessages(query);
        setSearchResult(messages);
    }

    return (
        <div>
            <form onSubmit={search}>
                <input type="text" name="query" value={query} onChange={(e) => setQuery(e.target.value)}/>
                <button>search</button>
            </form>
            {
                searchResult &&
                searchResult.map(m => <p key={m.id}>{m.content}</p>)
            }
                
        </div>
    )
}