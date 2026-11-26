function LibraryHeader() {
    const libraryHeader = "LIBRARY MANAGEMENT SYSTEM";

    const book = [
        { code: "SOD001", name: "Software Engineering", price: 100 },
        { code: "SOD002", name: "Software Engineering", price: 90 },
        { code: "FIN001", name: "Master Finance", price: 90 }
    ];

    const totalPrice = book.reduce((sum, b) => sum + b.price, 0);

    return (
        <div>
            <div><h1>{libraryHeader}</h1></div>
            <div><p>This is the Library management system.</p></div>
            <div><h2>List of available books</h2></div>
            <div>
                <ul>
                    {book.map(b => <li key={b.code}>Name: {b.name} - Price: ${b.price}</li>)}
                </ul>
            </div>
            <p>The total price for {book.length} books is total price ${totalPrice}</p>
        </div>
    );
}

export default LibraryHeader
