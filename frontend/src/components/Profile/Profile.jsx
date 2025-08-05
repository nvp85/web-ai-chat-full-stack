import './Profile.css';
import { useState } from 'react';
import ProfileTableRow from './ProfileTableRow';
import ErrorModal from '../Modal/ErrorModal';
import { isUsernameValid, isEmailValid } from '../../utils/utils';
import { useAuth } from '../../hooks/useAuth';
import LoadingMessage from '../LoadingMessage/LoadingMessage';

// user's profile page
export default function Profile() {
    const { currentUser, updateUser, handleUnauthorized, logout, token } = useAuth();
    const [errors, setErrors] = useState([]);
    // when an API call is in progress this text will be displayed in a pop-up
    const [loadingText, setLoadingText] = useState(null);

    // The function updates the user profile
    async function saveChanges(field, newValue) {
        const errors = [];
        // validation
        if (field === "username") {
            errors.push(...isUsernameValid(newValue));
        } else if (field === "email" && !isEmailValid(newValue)) {
            errors.push("Please enter a valid email.");
        }
        if (errors.length > 0) {
            setErrors(errors);
            return;
        }
        try {
            // if a user is changing their email they will be logged out
            // and should log in with the new email (they will need a new token)
            setLoadingText("Updating the profile");
            await updateUser({ ...currentUser, [field]: newValue }, token);
            if (field === "email") {
                logout();
            } else {
                setLoadingText(null);
            }
        } catch (err) {
            if (err.message === "Invalid credentials.") {
                // this function will send the user to the login page
                handleUnauthorized(); 
            } else {
                setLoadingText(null);
                setErrors([err.message]);
            }
        }
    }

    return (
        <div id="profile">
            <h1>User profile</h1>
            <table>
                <tbody>
                    <ProfileTableRow field="username" value={currentUser.username} saveChanges={saveChanges} />
                    <ProfileTableRow field="email" value={currentUser.email} saveChanges={saveChanges} />
                </tbody>
            </table>
            <p>Note: after editing your email you will be log out and will need to log in with the new email.</p>
            {errors.length > 0
                && <ErrorModal onClose={() => setErrors([])}>
                    {errors.map(error => <p className='red-text' key="{error}">{error}</p>)}
                </ErrorModal>}
            {loadingText &&
                <LoadingMessage text={loadingText} />}
        </div>
    )
}