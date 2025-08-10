import { NavLink } from 'react-router';
import { RiDeleteBinLine } from "react-icons/ri";
import { FiEdit3 } from "react-icons/fi";
import { useState, useEffect, useRef } from 'react';
import { TiTickOutline } from "react-icons/ti";
import { RiOpenaiFill } from "react-icons/ri";
import { RiGoogleFill } from "react-icons/ri";

// individual chat list item
export default function ChatListItem({ chat, deleteChat, renameChat }) {
    const [editing, setEditing] = useState(false);
    const [newTitle, setNewTitle] = useState(chat.title);
    const editImput = useRef();

    function openEditMode() {
        setEditing(true);
        setNewTitle(chat.title);
    }

    // submit the new title
    function handleSubmit(e) {
        e.preventDefault();
        renameChat(chat.id, newTitle);
        setEditing(false);
    }

    // The function exit edit mode on blur (click outside)
    // Blur event fires before Click event
    // so without the delay there is no way to click the submit button
    function exitEditMode() {
        setTimeout(() => setEditing(false), 200);
    }

    useEffect(() => {
        if (editing && editImput.current) {
            editImput.current.focus();
        }
    }, [editing])

    function getProviderIcon(chat) {
        switch (chat.llModel.provider) {
            case "OpenAI":
                return <RiOpenaiFill size="2rem"/>;
            case "Google":
                return <RiGoogleFill size="2rem"/>;
        }
    }

    return (<li className='chat-list-item'>
        {
            getProviderIcon(chat)
        }
        {editing
            ? (
                <form onSubmit={handleSubmit}>
                    <input
                        name='newTitle'
                        value={newTitle}
                        onChange={(e) => setNewTitle(e.target.value)}
                        onBlur={exitEditMode}
                        ref={editImput}
                        maxLength="50"
                    />
                    <button type="submit" className='icon-btn'><TiTickOutline size="2rem" /></button>
                </form>)
            : (
                <>
                    <span className='chat-title' title={chat.title}><NavLink to={`/chats/${chat.id}`} >{chat.title}</NavLink></span>
                    <button onClick={openEditMode} className='icon-btn'><FiEdit3 /></button>
                    <button
                        onClick={() => deleteChat(chat)}
                        value={chat.id}
                        className='icon-btn'><RiDeleteBinLine /></button>
                </>
            )}
    </li>)
}