import { register } from '../service/user.js'

const form = document.getElementById("form")
const nameInput = document.getElementById("name")
const lastnameInput = document.getElementById("last-name")
const userInput = document.getElementById("user")
const emailInput = document.getElementById("email")
const passwordInput = document.getElementById("password")
const _passwordInput = document.getElementById("_password")

//TOAST
const _successToast = document.getElementById('successToast')
const _errorToast = document.getElementById('errorToast')

form.addEventListener('submit',async(e)=>{
    e.preventDefault()

    const successToast = bootstrap.Toast.getOrCreateInstance(_successToast)
    const errorToast = bootstrap.Toast.getOrCreateInstance(_errorToast)

    const username = userInput.value
    const firstname = nameInput.value
    const lastname = lastnameInput.value
    const email = emailInput.value
    const password = passwordInput.value
    const _password = _passwordInput.value

    // validaciones
    if (
        username === '' || 
        password === '' ||
        _password === '' ||
        firstname === '' ||
        lastname === '' ||
        email === '' 
    ) {
        errorToast.show()
        return
    }

    //validacion password
    if (password !== _password){
        errorToast.show()
        return
    }

    // Si es valido, enviamos la peticion al backend
    const res = await register({
        username,
        email,
        password,
        name: {
            firstname,
            lastname
        }
    })
    console.log("Respuesta del servidor: ",res)

    if (res && res.id){
        successToast.show()

        setTimeout(()=>{
            window.location.href = '../../iniciar-sesion.html';
        }, 1000)
    } else{
        errorToast.show()
    }
})