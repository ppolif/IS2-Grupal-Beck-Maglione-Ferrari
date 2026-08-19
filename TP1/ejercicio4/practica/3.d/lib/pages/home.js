import { getAllProducts } from '../service/products.js'

const contenedor_pizzas = document.getElementById("contenedor-pizzas")
const contenedor_empanadas = document.getElementById("contenedor-empanadas")
const contenedor_bebidas = document.getElementById("contenedor-bebidas")

const fillProducts = async()=>{
    const products = await getAllProducts()
    
    products.forEach(product => {
        const category = product.category

        let container
        if (category == "men's clothing"){
            container = contenedor_pizzas
        } else if(category == "jewelery"){
            container = contenedor_empanadas
        } else if (category == "electronics"){
            container = contenedor_bebidas
        }
        if (container){
            container.innerHTML += 
                    `<div class="col">
                        <div class="card h-100">
                            <img src="${product.image}" class="card-img-top">
                            <div class="card-body p-4">
                                <div class="text-center">
                                    <h5 class="fw-bolder">${product.title}</h5>
                                    <span>$${product.price}</span>
                                </div>
                            </div>
                            <div class="card-footer p-4 pt-0 border-top-0 bg-transparent">
                                <div class="text-center">
                                    <a href="" class="btn btn-outline-success mt-auto">
                                        Aniadir al carrito
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>`
        }
    })
}

fillProducts()