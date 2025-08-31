import './AboutPage.css';

export default function AboutPage() {

    return (
        <div id="about">
            <h1>About this app</h1>
            <p>This project is a full-stack web application built as part of the LaunchCode coding bootcamp to practice React and the Spring framework.
                It offers a unified web interface for interacting with LLMs from different AI providers.
                Currently, the app supports two LMMs: OpenAI gpt-4o-mini and Google's Gemini flash, so users can experiment without switching tools.</p>

            <p>Core features include:</p>
            <ul>
                <li>Token-based authentication (no refresh tokens for simplicity)</li>

                <li>Create and manage chats: start new conversations, rename or delete existing ones.</li>

                <li>Automatically generated chat titles: the app attempts to generate a title from the first message, with a fallback if the API call fails.</li>
                <li>Search across the chats by keywords and meaning.</li>
            </ul> 
        </div >
    )
}