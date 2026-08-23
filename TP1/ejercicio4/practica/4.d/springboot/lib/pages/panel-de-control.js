import { getAllProducts } from '../service/products.js' 

const productTableBody = document.getElementById("productTableBody")

const handleCategoryColor = (category) => {
    let color = "light"

    switch (category) {
        case "men's clothing":
            color = "bg-danger"
            break
        case "jewelery":
            color = "bg-info"
            break;
        case "electronics":
            color = "bg-warning"
            break;
        case "women's clothing":
            color = "bg-info"
            break;
        default:
            break;
    }

    return color
} 

const fillProductTable = async ()=>{
    const products = await getAllProducts()

    products.forEach(product => {
        productTableBody.innerHTML +=
        `<tr>
            <td>
                <div class="d-flex align-items-center gap-2">
                    <img
                        class="rounded-circle"
                        src="${product.image}"
                        alt=""
                        style="width: 45px; height: 45px;"
                    >
                    <span class="fw-bold text-nowrap">
                        ${product.title}
                    </span>
                </div>
            </td>
            <td>
                <span class="badge ${handleCategoryColor(product.category)} text-dark">
                    ${product.category}
                </span>
            </td>
            <td>
                <span class="badge bg-success rounded-pill">
                    $${product.price}
                </span>
            </td>
            <td>
                <div class="d-flex gap-1">
                    <button
                        type="button"
                        class="btn btn-outline-primary btn-sm fw-bold"
                    >Editar</button>
                </div>
                <div class="d-flex gap-1">
                    <button
                        type="button"
                        class="btn btn-outline-primary btn-sm fw-bold"
                    ><i class="bi bi-trash3"></i></button>
                </div>
            </td>
        </tr>`
    })
}

fillProductTable()