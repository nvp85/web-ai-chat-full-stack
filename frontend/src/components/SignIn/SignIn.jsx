import { Link, useNavigate, Navigate } from 'react-router';
import { useState } from "react";
import { LuEye, LuEyeOff } from "react-icons/lu";
import './SignIn.css';
import { useUser } from '../../hooks/useUser';
import { isEmailValid } from '../../utils/utils';

export default function SignIn() {
    const [formData, setFormData] = useState({
        email: "demoUser@example.com", 
        password: "qwerty123"
    });
    const [showPassword, setShowPassword] = useState(false);
    const manageUser = useUser();
    const [error, setError] = useState("");
    const navigate = useNavigate();

    // if there is an info in the local storage then user is logged in
    if (manageUser.currentUser) {
        return <Navigate to='/' />
    }

    function handleSubmit(e) {
        e.preventDefault();
        if (!formData.email || !formData.password) {
            setError("Please enter your email and password.");
            return;
        };
        if (!isEmailValid(formData.email)) {
            setError("Please enter a valid email address.");
            return;
        };
        try {
            manageUser.login(formData.email, formData.password);  
            navigate("/");  
        } catch (err) {
            setError(err.message);
        }
    }

    function handleChange(e) {
        const { name, value } = e.target;
        setFormData(prev => ({...prev, [name]: value}));
    }

    function togglePassword(e) {
        e.preventDefault();
        setShowPassword(prev => !prev);
    }

    return (
        <div className="form-container">
            <h1>Sign In</h1>
            <p>Don't have an accout? Register <Link to="/register">here</Link>.</p>
            {error ? <p className="red-text">{error}</p> : "" }
            <form id="login-form" className="form" onSubmit={handleSubmit}>
                <input 
                    type="email"
                    name="email"
                    value={formData.email}
                    placeholder="Email"
                    onChange={handleChange}
                    maxLength="150"
                    required
                    />
                <input 
                    type={showPassword ? "text" : "password"} 
                    name="password"
                    value={formData.password}
                    onChange={handleChange}
                    maxLength="150"
                    required
                    placeholder="Password"/>
                <button type="button" className="eye-btn" onClick={togglePassword}>
                    {showPassword ? <LuEye /> : <LuEyeOff /> }
                </button>
                <button type="submit" className="btn" id="login-btn">submit</button>
            </form>
        </div>
    )
}