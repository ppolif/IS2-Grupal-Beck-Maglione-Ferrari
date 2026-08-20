const form = document.getElementById("form")
const userInput = document.getElementById("user")
const passwordInput = document.getElementById("password")

form.addEventListener('submit',(e)=>{
    e.preventDefault()

    const username = userInput.value
    const password = passwordInput.value

    if (username === '' || password === ''){
        alert('Completa todos los campos')
        return
    }

    
})