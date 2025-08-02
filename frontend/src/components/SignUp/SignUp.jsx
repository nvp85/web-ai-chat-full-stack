import { Link } from 'react-router';
import './SignUp.css';
import { useState } from "react";
import { useNavigate } from 'react-router';
import { LuEye, LuEyeOff } from "react-icons/lu";
import { isPasswordValid, isEmailValid, isUsernameValid } from '../../utils/utils';
import { useAuth } from '../../hooks/useAuth';
import Modal from '../Modal/Modal';
import ErrorModal from '../Modal/ErrorModal';
import LoadingMessage from '../LoadingMessage/LoadingMessage';

// handles registration
export default function SignUp() {
    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
    });
    const [formErrors, setFormErrors] = useState([]);
    const navigate = useNavigate();
    const [showPassword, setShowPassword] = useState(false);
    const auth = useAuth();
    // this state is for the modal that will inform the user
    // when an account is created successfully
    const [modalOpen, setModalOpen] = useState(false);

    // this is for the error modal that will show errors if auto login fails
    const [errorModalOpen, setErrorModalOpen] = useState(false);

    // this is for the loading message component 
    // that shows up to indicate that an API call in progress
    const [loadingText, setLoadingText] = useState(null);

    // creates a new account
    async function handleSignup(e) {
        e.preventDefault();
        const errors = [];
        // validation
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
        setLoadingText("Creating an account");
        try {
            const user = {
                name: formData.name,
                email: formData.email,
                password: formData.password,
            }
            await auth.register(user);
            setModalOpen(true);
        } catch (err) {
            setFormErrors([err.message]);
        } finally {
            setLoadingText(null);
        }
    }

    function handleChange(e) {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    }

    // for better UX this function will log the user in right after the account is created
    async function autoLogin() {
        setModalOpen(false);
        try {
            setLoadingText("Logging in");
            await auth.login(formData.email, formData.password);
            navigate("/");
        } catch (err) {
            setLoadingText(null);
            setErrorModalOpen(true);
        }
    }

    return (
        <>
            <div className="form-container">
                <h1>Sign Up</h1>
                <p>Already have an accout? Sign in <Link to="/login">here</Link>.</p>
                {formErrors.map(err => <p className="red-text" key={err}>{err}</p>)}
                <form id="register-form" className="form" onSubmit={handleSignup}>
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
                        {showPassword ? <LuEye /> : <LuEyeOff />}
                    </button>
                    <button type='submit' id="signup-btn" className='btn'>submit</button>
                </form>
            </div>
            {modalOpen &&
                <Modal onClose={() => setModalOpen(false)}>
                    <p>Account has been created successfully.</p>
                    <button className='btn' onClick={autoLogin}>Login</button>
                </Modal>}
            {errorModalOpen &&
                <ErrorModal onClose={setErrorModalOpen(false)}>
                    <p>Failed to login, please try again later.</p>
                </ErrorModal>}
            {loadingText &&
                <LoadingMessage text={loadingText} />
            }
        </>
    )
}