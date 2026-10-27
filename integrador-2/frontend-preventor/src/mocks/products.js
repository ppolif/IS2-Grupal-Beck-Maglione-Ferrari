export const candyImg =
  'https://images.unsplash.com/photo-1633158832466-be592c721217?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&q=85&w=1200';
export const macaronsImg =
  'https://images.unsplash.com/photo-1666274694243-9997eb427237?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&q=85&w=1200';

export const productsMock = [
  {
    id: 1,
    nombre: 'Alfajor triple chocolate',
    presentacion: 'Caja x 24 u.',
    precio: '$18.900',
    precioNum: 18900,
    categoria: 'Alfajores',
    imagen: macaronsImg,
    stock: 128
  },
  {
    id: 2,
    nombre: 'Gomitas frutales',
    presentacion: 'Bolsa x 1 kg',
    precio: '$6.450',
    precioNum: 6450,
    categoria: 'Golosinas',
    imagen: candyImg,
    stock: 84
  },
  {
    id: 3,
    nombre: 'Chupetines surtidos',
    presentacion: 'Display x 50 u.',
    precio: '$8.200',
    precioNum: 8200,
    categoria: 'Chupetines',
    imagen: candyImg,
    stock: 96
  },
  {
    id: 4,
    nombre: 'Chocolate con maní',
    presentacion: 'Caja x 20 u.',
    precio: '$15.600',
    precioNum: 15600,
    categoria: 'Chocolates',
    imagen: macaronsImg,
    stock: 45
  }
];

export const cartItemsMock = [
  {
    id: 1,
    nombre: 'Alfajor triple chocolate',
    presentacion: 'Caja x 24 u.',
    precio: '$18.900',
    precioNum: 18900,
    cantidad: 2,
    imagen: macaronsImg
  },
  {
    id: 2,
    nombre: 'Gomitas frutales',
    presentacion: 'Bolsa x 1 kg',
    precio: '$6.450',
    precioNum: 6450,
    cantidad: 1,
    imagen: candyImg
  }
];

