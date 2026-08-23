export function login({username, password}){
    return fetch('https://fakestoreapi.com/auth/login', {
    method: 'POST',
    headers: {
        "Content-Type": "application/json"
    },
    body: JSON.stringify({
        username,
        password
    })
    })
    .then(response =>  {
        if (!response.ok){
            console.log(response)
            throw new Error('Usuario y/o contraseña incorrecta')
        }
        return response.json()
    })
    .catch(e=>e)
}

export function register({username, email, firstname, lastname ,password}){
    return fetch('https://fakestoreapi.com/users', {
    method: 'POST',
    headers: {
            "Content-Type": "application/json"
    },
    body: JSON.stringify({
        email,
        username,
        password,
        name:{
            firstname,
            lastname
        }
    })
    })
    .then(responde => responde.json())
    .then(data => data)
    .catch(error => {
        console.log("Error capturado:", error.message)
        return { error: true }
    });
}