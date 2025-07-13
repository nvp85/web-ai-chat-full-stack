import Modal from "./Modal";

export default function ErrorModal({children, onClose}) {
    return (
        <Modal onClose={onClose} btnText='Close'>
            <h3>Error</h3>
            {children}
        </Modal>
    )
}