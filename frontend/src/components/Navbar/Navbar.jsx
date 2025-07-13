import { NavLink, useNavigate } from "react-router";
import './Navbar.css'
import { useState, useEffect, useRef } from "react";
import { useUser } from '../../hooks/useUser';
import { IoHomeOutline } from "react-icons/io5";
import Hamburger from 'hamburger-react';

export default function Navbar() {
    const user = useUser();
    const isAuthenticated = user.currentUser ? true : false;
    const [isMenuOpen, setIsMenuOpen] = useState();
    const menuRef = useRef();
    const navigate = useNavigate();

    function handleLogout() {
        navigate("/");
        user.logout();
    }
    
    return (
        <nav>
            <div className="nav-links">
                <NavLink to="/"><IoHomeOutline size="2rem" /> Home</NavLink>
            </div>
            <div className="nav-links" id="desktop-nav">
                <NavLink to="about">About</NavLink>
                {isAuthenticated
                    ? (<>
                        <NavLink to="chats" id="chats-link">Chats</NavLink>
                        {
                            user.currentUser.name.trim()
                                ? <NavLink to="profile">Welcome, {user.currentUser.name}</NavLink>
                                : <NavLink to="profile">Profile</NavLink>
                        }
                        <button onClick={user.logout} className="btn">Logout</button>
                    </>)
                    : (<>
                        <NavLink to="login">Sign In</NavLink>
                        <NavLink to="register">Sign Up</NavLink>
                    </>)
                }
            </div>
            <div id="mobil-nav">
                <div>
                    <Hamburger toggled={isMenuOpen} size={22} toggle={setIsMenuOpen} />
                    {isMenuOpen &&
                        <div className="nav-links"
                            id="hamburger-menu"
                            ref={menuRef}>
                            <NavLink to="about">About</NavLink>
                            {isAuthenticated
                                ? (<>
                                    <NavLink to="chats" id="chats-link">Chats</NavLink>
                                    <NavLink to="profile">Profile</NavLink>
                                    <button onClick={handleLogout} className="btn">Logout</button>
                                </>)
                                : (<>
                                    <NavLink to="login">Sign In</NavLink>
                                    <NavLink to="register">Sign Up</NavLink>
                                </>)
                            }
                        </div>}
                </div>
            </div>
        </nav>
    )
}