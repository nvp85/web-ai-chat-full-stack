import { useState, useRef, useEffect } from 'react';
import { FiEdit3 } from "react-icons/fi";
import { TiTickOutline } from "react-icons/ti";

// Individual row of the table in the Profile component
// It displays a user's field (username or email) and an edit button
export default function ProfileTableRow(props) {
    const { field, value, saveChanges } = props;
    const [editing, setEditing] = useState(false);
    const [newValue, setNewValue] = useState(value);
    // the ref is for focusing on the input field in the edit mode
    const editImput = useRef(); 

    // the actual logic is in the Profile component
    function handleSave() {
        saveChanges(field, newValue);
        setEditing(false);
    }

    function handleEdit() {
        setEditing(true);
        setNewValue(value);
    }

    useEffect(() => {
        if (editing && editImput.current) {
            editImput.current.focus();
        }
    }, [editing])

    return (
        <tr>
            <th>{field[0].toUpperCase() + field.slice(1)}:</th>
            <td>
                {editing
                    ? <input
                        name="newValue"
                        value={newValue}
                        onChange={(e) => setNewValue(e.target.value)}
                        onBlur={() => setEditing(false)}
                        ref={editImput}
                    />
                    : value}
            </td>
            <td>
                {
                    editing
                        ? <button
                            onClick={handleSave}
                            onMouseDown={(e) => e.preventDefault()}
                            className='icon-btn'><TiTickOutline /></button>
                        : <button
                            onClick={handleEdit}
                            className='icon-btn'><FiEdit3 /></button>
                }
            </td>
        </tr>

    )
}