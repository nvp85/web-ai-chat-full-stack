import { NavLink } from 'react-router';
import { RiDeleteBinLine } from "react-icons/ri";
import { FiEdit3 } from "react-icons/fi";
import { useState, useEffect, useRef } from 'react';
import { TiTickOutline } from "react-icons/ti";


export default function ChatListItem({ chat, deleteChat, rename }) {
    const [editing, setEditing] = useState(false);
    const [newTitle, setNewTitle] = useState(chat.title);
    const editImput = useRef();

    function handleClick() {
        setEditing(true);
        setNewTitle(chat.title);
    }

    function handleSubmit(e) {
        e.preventDefault();
        rename(chat.id, newTitle);
        setEditing(false);
    }

    function handleBlur() {
        setTimeout(() => setEditing(false), 200);
    }

    useEffect(() => {
        if (editing && editImput.current) {
            editImput.current.focus();
        }
    }, [editing])

    return (<li className='chat-list-item'>
        {editing
            ? (
                <form onSubmit={handleSubmit}>
                    <input
                        name='newTitle'
                        value={newTitle}
                        onChange={(e) => setNewTitle(e.target.value)}
                        onBlur={handleBlur}
                        ref={editImput}
                        maxLength="50"
                    />
                    <button type="submit" className='icon-btn'><TiTickOutline size="2rem" /></button>
                </form>)
            : (
                <>
                    <span className='chat-title'><NavLink to={`/chats/${chat.id}`} >{chat.title}</NavLink></span>
                    <button onClick={handleClick} className='icon-btn'><FiEdit3 /></button>
                    <button
                        onClick={() => deleteChat(chat)}
                        value={chat.id}
                        className='icon-btn'><RiDeleteBinLine /></button>
                </>
            )}
    </li>)
}