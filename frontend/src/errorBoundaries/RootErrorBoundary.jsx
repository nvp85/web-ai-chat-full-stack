import React from "react";

// Error boundary catches errors and displays an error message
export default class RootErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error: error };
    }

    componentDidCatch(error, errorInfo) {
        if (error.message.includes("JSON.parse")) {
            // if there is some garbage instead of a token
            localStorage.removeItem("auth-token");
        }
    }

    render() {
        if (this.state.hasError) {
            // render custom fallback UI
            return (<>
                <h1 style={{ textAlign: 'center' }}>Oops! Something went wrong.</h1>
                <p style={{ textAlign: 'center' }}><a href="/">Go Home</a></p>
            </>);
        }

        return this.props.children;
    }
}