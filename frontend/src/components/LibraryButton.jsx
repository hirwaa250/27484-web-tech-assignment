function LibraryButton({ buttonLabel, buttonAction }) {
    return (
        <button onClick={buttonAction}>{buttonLabel}</button>
    );
}

export default LibraryButton
