import { getOneProduct, getProductsInCategory } from '../service/products.js'

const id = new URLSearchParams(window.location.search).get('id')

//inicializar elementos
const product_image = document.getElementById("product-image")
const product_title = document.getElementById("product-title")
const product_price = document.getElementById("product-price")
const product_description = document.getElementById("product-description")

const productosRelacionadosContainer = document.getElementById("productos-relacionados")

const fillProductosRelacionados= async(category)=>{
    const products = await getProductsInCategory(category)
        
        products.forEach(product => {
    
                productosRelacionadosContainer.innerHTML += 
                        `<div class="col">
                            <div class="card h-100">
                                <img src="${product.image}" class="card-img-top" style="min-height: 300px; max-height:300px;">
                                <div class="card-body p-4">
                                    <div class="text-center">
                                        <h5 class="fw-bolder">${product.title}</h5>
                                        <span>$${product.price}</span>
                                    </div>
                                </div>
                                <div class="card-footer p-4 pt-0 border-top-0 bg-transparent">
                                    <div class="text-center d-flex gap-1 justify-content-center">
                                        <a href="/detalle.html?id=${product.id}" class="btn btn-outline-secondary mt-auto">
                                            Ver mas
                                        </a>
                                        <a href="" class="btn btn-outline-success mt-auto">
                                            Añadir al carrito
                                        </a>
                                    </div>
                                </div>
                            </div>
                        </div>`
            }
        )
}

const fillDetailProduct = async ()=>{
    const product = await getOneProduct(id)

    if (product) {
        product_image.src = product.image
        product_title.innerText = product.title
        product_price.innerText = `$${product.price}`
        product_description.innexText = product.product_description

        fillProductosRelacionados(product.category)
    }
}

fillDetailProduct()