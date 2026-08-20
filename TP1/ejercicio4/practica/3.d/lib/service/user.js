export function login({username, password}){
    fetch('https://fakestoreapi.com/auth/login', {
    method: 'POST',
    body: JSON.stringify({
        username,
        password
    })
    })
    .then(response => response.json())
    .then(data => console.log(data));
}