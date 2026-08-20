export function getAllProducts (){
    return fetch('https://fakestoreapi.com/products')
  .then(response => response.json())
  .then(data => data);
}

export function getOneProduct(id){
   return fetch(`https://fakestoreapi.com/products/${id}`)
  .then(response => response.json())
  .then(data => data);
}

export function getProductsInCategory(category){
   return fetch(`https://fakestoreapi.com/products/category/${category}`)
  .then(response => response.json())
  .then(data => data);
}