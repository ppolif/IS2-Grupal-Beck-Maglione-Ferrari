import { login } from '../service/user.js'

const form = document.getElementById("form")
const userInput = document.getElementById("user")
const passwordInput = document.getElementById("password")

//TOAST
const _successToast = document.getElementById('successToast')
const _errorToast = document.getElementById('errorToast')

form.addEventListener('submit',async(e)=>{
    e.preventDefault()

    const successToast = bootstrap.Toast.getOrCreateInstance(_successToast)
    const errorToast = bootstrap.Toast.getOrCreateInstance(_errorToast)

    const username = userInput.value
    const password = passwordInput.value

    // validaciones
    if (username === '' || password === ''){
        errorToast.show()
        return
    }

    // Si es valido
    const res = await login({username,password})

    if (res.token){
        successToast.show()

        setTimeout(()=>{
            window.location.href = '/';
        }, 1000)
    } else{
        errorToast.show()
    }
})