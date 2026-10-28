import axios   from 'axios';

const API_URL = 'http://localhost:8080/api/books';

class BookService {
    getAllBooks() {
        return axios.get(`${API_URL}/getAllBooks`);
    }

    addBook(book) {
        return axios.post(`${API_URL}/addBook`, book);
    }

    updateBook(book) {
        return axios.put(`${API_URL}/updateBook/${id}`, book);
    }
    
    deleteBook(bookId) {
        return axios.delete(`${API_URL}/deleteBook/${Id}`);
    }
}

export default new BookService();