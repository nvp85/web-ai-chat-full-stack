
export function isEmailValid(email) {
    if(!/\S+@\S+\.\S+/.test(email)) {
        return false;
    }
    return true;
}

export function isUsernameValid(name) {
    const errors = [];
    if (!name) {
        return [];
    }
    if (name.length > 150) {
        errors.push("Name should not be longer than 150 characters.");
    }
    if (!/^[A-Za-z0-9_' ]+$/.test(name)) {
        errors.push("Name can only contain letters, numbers, spaces, underscores, and apostrophes.");
    }
    return errors;
}

export function isPasswordValid(password) {
    const errors = [];
    if (password.length < 8) {
        errors.push("Password should be at least 8 characters long.");
    };
    if (!/^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*()_+{}":;'<>?,./]).*$/.test(password)) {
        errors.push("Password should include at least one uppercase and one lowercase letter, at least one number, and at least one special character.");
    };
    if (/^(?=.*\s).*$/.test(password)) {
        errors.push("Password should not contain whitespaces.");
    };
    return errors;
}
