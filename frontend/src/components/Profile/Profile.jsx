import './Profile.css';
import { useState } from 'react';
import { useUser } from '../../hooks/useUser';
import ProfileTableRow from './ProfileTableRow';
import ErrorModal from '../Modal/ErrorModal';
import { isUsernameValid, isEmailValid } from '../../utils/utils';

export default function Profile() {
    const { currentUser, saveUser } = useUser();
    const [errors, setErrors] = useState([]);

    function saveChanges(field, newValue) {
        const errors = [];
        if (field === "name") {
            errors.push(...isUsernameValid(newValue));
        } else if (field === "email" && !isEmailValid(newValue)) {
            errors.push("Please enter a valid email.");
        }
        if (errors.length > 0) {
            setErrors(errors);
            return;
        }
        try {
            saveUser({ ...currentUser, [field]: newValue });
        } catch {
            setErrors(["Something went wrong. Failed to save the changes."])
        }

    }

    return (
        <div id="profile">
            <h1>User profile</h1>
            <table>
                <tbody>
                    <ProfileTableRow field="name" value={currentUser.name} saveChanges={saveChanges} />
                    <ProfileTableRow field="email" value={currentUser.email} saveChanges={saveChanges} />
                </tbody>
            </table>
            {errors.length > 0
                && <ErrorModal onClose={() => setErrors([])}>
                    {errors.map(error => <p className='red-text'>{error}</p>)}
                </ErrorModal>}
        </div>
    )
}