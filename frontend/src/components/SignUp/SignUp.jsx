import { Link } from 'react-router';
import './SignUp.css';
import { useState } from "react";
import { useNavigate } from 'react-router';
import { LuEye, LuEyeOff } from "react-icons/lu";
import { useUser } from '../../hooks/useUser';
import { isPasswordValid, isEmailValid, isUsernameValid } from '../../utils/utils';


export default function SignUp() {
    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
    });
    const [formErrors, setFormErrors] = useState([]);
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const manageUser = useUser();

    // validate the form
    // when a user is created - just save it into the local storage
    function handleSubmit(e) {
        e.preventDefault();
        const errors = [];
        if (!formData.email || !formData.password) {
            errors.push("Please enter valid email and password.");
        } else if (!isEmailValid(formData.email)) {
            errors.push("Please enter a valid email address.");
        };
        errors.push(...isUsernameValid(formData.name));
        errors.push(...isPasswordValid(formData.password));
        if (errors.length > 0) {
            setFormErrors(errors);
            return;
        }

        // try to create an acc
        try {
            const user = {
                name: formData.name,
                email: formData.email,
                password: formData.password,
            }
            manageUser.register(user);
            navigate("/");
        } catch (err) {
            setFormErrors([err.message]);
        }
    }
    function handleChange(e) {
        const { name, value } = e.target;
        setFormData(prev => ({...prev, [name]: value}));
    }

    return (
        <div className="form-container">
            <h1>Sign Up</h1>
            <p>Already have an accout? Sign in <Link to="/login">here</Link>.</p>
            {formErrors.map(err => <p className="red-text" key={err}>{err}</p>)}
            <form id="register-form" className="form" onSubmit={handleSubmit}>
                <input 
                    type="name" 
                    placeholder="Name (optional)"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    maxLength="150"
                    />
                <input 
                    type="email" 
                    placeholder="Email" 
                    name='email'
                    value={formData.email}
                    onChange={handleChange}
                    maxLength="150"
                    required
                    />
                <input 
                    type={showPassword ? "text" : "password"}
                    placeholder="Password" 
                    name='password'
                    value={formData.password}
                    onChange={handleChange}
                    maxLength="150"
                    required
                    />
                <button type="button" className="eye-btn" onClick={() => setShowPassword(prev => !prev)}>
                    {showPassword ? <LuEye /> : <LuEyeOff /> }
                </button>
                <button type='submit' id="signup-btn" className='btn'>submit</button>
            </form>
        </div>
    )
}