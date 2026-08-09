// Función para navegar entre las pantallas del Navbar
function mostrarPantalla(idPantalla, elementoLink) {
    // 1. Ocultar todas las pantallas
    const pantallas = document.querySelectorAll('.pantalla-abm');
    pantallas.forEach(pantalla => pantalla.classList.remove('activa'));

    // 2. Mostrar la pantalla solicitada
    document.getElementById(idPantalla).classList.add('activa');

    // 3. Actualizar el estado visual del Navbar
    const links = document.querySelectorAll('.nav-link');
    links.forEach(link => link.classList.remove('active', 'fw-bold')); // Quita activo a todos
    
    // Le da estilo activo al link presionado
    if(elementoLink) {
        elementoLink.classList.add('active', 'fw-bold'); 
    }
}

// ==========================================
// 1. ESTADO DE LA APLICACIÓN
// ==========================================
let empresas = [];
let sucursales = [];
let empleados = [];
// Función auxiliar para validar que un valor contenga SOLO números enteros positivos
//Utiliza una expresion regular, el patron se delimita entre los //, el ^ indica el comienzo del string, \d indica un digito, + indica que puede haber uno o mas digitos, y $ indica el final del string.
const esSoloNumeros = (valor) => /^\d+$/.test(valor);

// Variables para saber qué registro estamos editando y manejar los modales
let idEdicionEmpresa = null, modalEmpresa = null;
let idEdicionSucursal = null, modalSucursal = null;
let idEdicionEmpleado = null, modalEmpleado = null;

// ==========================================
// 2. NAVEGACIÓN
// ==========================================
function mostrarPantalla(idPantalla, elementoLink) {
    document.querySelectorAll('.pantalla-abm').forEach(p => p.classList.remove('activa'));
    document.getElementById(idPantalla).classList.add('activa');
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active', 'fw-bold'));
    if(elementoLink) elementoLink.classList.add('active', 'fw-bold');
}

// ==========================================
// 3. LÓGICA DE EMPRESAS
// ==========================================
const formEmpresa = document.getElementById('formEmpresa');
const tablaEmpresas = document.getElementById('tablaEmpresas');
const selectEmpresa = document.getElementById('selectEmpresa');

formEmpresa.addEventListener('submit', (e) => {
    e.preventDefault();
    const razonSocial = document.getElementById('razonSocial').value.trim();
    const cuit = document.getElementById('cuit').value.trim();

    if (!razonSocial || !cuit) return alert('Completá todos los campos de la Empresa.');
    
    // NUEVA VALIDACIÓN
    if (!esSoloNumeros(cuit)) return alert('Error: El CUIT debe contener solo números (sin guiones ni espacios).');

    empresas.push({ id: Date.now(), razonSocial, cuit });
    actualizarUIEmpresas();
    formEmpresa.reset();
});

function actualizarUIEmpresas() {
    if (empresas.length === 0) {
        tablaEmpresas.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">No hay empresas registradas.</td></tr>';
    } else {
        tablaEmpresas.innerHTML = empresas.map(emp => `
            <tr>
                <td class="align-middle">${emp.razonSocial}</td>
                <td class="align-middle">${emp.cuit}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-warning me-1" onclick="abrirModalEmpresa(${emp.id})">Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarEmpresa(${emp.id})">Eliminar</button>
                </td>
            </tr>
        `).join('');
    }

    selectEmpresa.innerHTML = '<option value="">Seleccione primero una empresa...</option>' + 
        empresas.map(emp => `<option value="${emp.id}">${emp.razonSocial}</option>`).join('');
        
    actualizarUISucursales(); // Redibuja en cascada por si cambió algún nombre
}

function eliminarEmpresa(id) {
    empresas = empresas.filter(emp => emp.id !== id);
    actualizarUIEmpresas();
}

// Edición de Empresa
function abrirModalEmpresa(id) {
    idEdicionEmpresa = id;
    const emp = empresas.find(e => e.id === id);
    
    document.getElementById('editRazonSocial').value = emp.razonSocial;
    document.getElementById('editCuit').value = emp.cuit;
    
    if(!modalEmpresa) modalEmpresa = new bootstrap.Modal(document.getElementById('modalEditarEmpresa'));
    modalEmpresa.show();
}

function guardarEdicionEmpresa() {
    const razon = document.getElementById('editRazonSocial').value.trim();
    const cuit = document.getElementById('editCuit').value.trim();
    
    if (!razon || !cuit) return alert('Completá todos los campos.');
    
    if (!esSoloNumeros(cuit)) return alert('Error: El CUIT debe contener solo números (sin guiones ni espacios).');

    const emp = empresas.find(e => e.id === idEdicionEmpresa);
    emp.razonSocial = razon;
    emp.cuit = cuit;

    modalEmpresa.hide();
    actualizarUIEmpresas();
}
// ==========================================
// 4. LÓGICA DE SUCURSALES
// ==========================================
const formSucursal = document.getElementById('formSucursal');
const tablaSucursales = document.getElementById('tablaSucursales');
const selectSucursal = document.getElementById('selectSucursal');

// Función auxiliar para armar el texto completo de la dirección
function formatearDireccion(dir) {
    return `${dir.calle} ${dir.altura}, ${dir.localidad}, ${dir.departamento}, ${dir.provincia}, ${dir.pais}`;
}

formSucursal.addEventListener('submit', (e) => {
    e.preventDefault();
    const empresaId = document.getElementById('selectEmpresa').value;
    
    const direccionObj = {
        calle: document.getElementById('calleSucursal').value.trim(),
        altura: document.getElementById('alturaSucursal').value.trim(),
        localidad: document.getElementById('localidadSucursal').value.trim(),
        departamento: document.getElementById('departamentoSucursal').value.trim(),
        provincia: document.getElementById('provinciaSucursal').value.trim(),
        pais: document.getElementById('paisSucursal').value.trim()
    };

    if (!empresaId || !direccionObj.calle || !direccionObj.altura || !direccionObj.localidad || 
        !direccionObj.departamento || !direccionObj.provincia || !direccionObj.pais) {
        return alert('Por favor, seleccioná la empresa y completá todos los campos de la dirección.');
    }

    if (!esSoloNumeros(direccionObj.altura)) return alert('Error: La altura de la calle debe ser un número entero.');

    sucursales.push({ id: Date.now(), empresaId: Number(empresaId), direccion: direccionObj });
    actualizarUISucursales();
    formSucursal.reset();
});

function actualizarUISucursales() {
    if (sucursales.length === 0) {
        tablaSucursales.innerHTML = '<tr><td colspan="3" class="text-center text-muted py-3">No hay sucursales registradas.</td></tr>';
    } else {
        tablaSucursales.innerHTML = sucursales.map(suc => {
            const empresaDuena = empresas.find(emp => emp.id === suc.empresaId);
            const nombreEmpresa = empresaDuena ? empresaDuena.razonSocial : 'Empresa eliminada';
            
            // Usamos la función auxiliar para mostrar la dirección de forma legible
            const textoDireccion = formatearDireccion(suc.direccion);

            return `
            <tr>
                <td class="align-middle">${nombreEmpresa}</td>
                <td class="align-middle small">${textoDireccion}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-warning me-1" onclick="abrirModalSucursal(${suc.id})">Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarSucursal(${suc.id})">Eliminar</button>
                </td>
            </tr>`;
        }).join('');
    }

    // Actualizamos el select de Empleados con el formato amigable
    selectSucursal.innerHTML = '<option value="">Seleccione primero una sucursal...</option>' + 
        sucursales.map(suc => {
            const emp = empresas.find(e => e.id === suc.empresaId);
            const nombreEmp = emp ? emp.razonSocial : '';
            return `<option value="${suc.id}">${suc.direccion.calle} ${suc.direccion.altura} - ${suc.direccion.localidad} (${nombreEmp})</option>`;
        }).join('');
        
    actualizarUIEmpleados(); // Redibuja en cascada
}

function eliminarSucursal(id) {
    sucursales = sucursales.filter(suc => suc.id !== id);
    actualizarUISucursales();
}

// Edición de Sucursal
function abrirModalSucursal(id) {
    idEdicionSucursal = id;
    const suc = sucursales.find(s => s.id === id);
    
    const selectEdit = document.getElementById('editSelectEmpresa');
    selectEdit.innerHTML = empresas.map(emp => `<option value="${emp.id}">${emp.razonSocial}</option>`).join('');
    selectEdit.value = suc.empresaId;
    
    document.getElementById('editCalleSucursal').value = suc.direccion.calle;
    document.getElementById('editAlturaSucursal').value = suc.direccion.altura;
    document.getElementById('editLocalidadSucursal').value = suc.direccion.localidad;
    document.getElementById('editDepartamentoSucursal').value = suc.direccion.departamento;
    document.getElementById('editProvinciaSucursal').value = suc.direccion.provincia;
    document.getElementById('editPaisSucursal').value = suc.direccion.pais;
    
    if(!modalSucursal) modalSucursal = new bootstrap.Modal(document.getElementById('modalEditarSucursal'));
    modalSucursal.show();
}

function guardarEdicionSucursal() {
    const empresaId = document.getElementById('editSelectEmpresa').value;
    
    const dirEditada = {
        calle: document.getElementById('editCalleSucursal').value.trim(),
        altura: document.getElementById('editAlturaSucursal').value.trim(),
        localidad: document.getElementById('editLocalidadSucursal').value.trim(),
        departamento: document.getElementById('editDepartamentoSucursal').value.trim(),
        provincia: document.getElementById('editProvinciaSucursal').value.trim(),
        pais: document.getElementById('editPaisSucursal').value.trim()
    };

    if (!empresaId || !dirEditada.calle || !dirEditada.altura || !dirEditada.localidad || 
        !dirEditada.departamento || !dirEditada.provincia || !dirEditada.pais) {
        return alert('Completá todos los campos.');
    }
    
    if (!esSoloNumeros(dirEditada.altura)) return alert('Error: La altura de la calle debe ser un número entero.');

    const suc = sucursales.find(s => s.id === idEdicionSucursal);
    suc.empresaId = Number(empresaId);
    suc.direccion = dirEditada;

    modalSucursal.hide();
    actualizarUISucursales();
}
// ==========================================
// 5. LÓGICA DE EMPLEADOS
// ==========================================
const formEmpleado = document.getElementById('formEmpleado');
const tablaEmpleados = document.getElementById('tablaEmpleados');

formEmpleado.addEventListener('submit', (e) => {
    e.preventDefault();
    const sucursalId = document.getElementById('selectSucursal').value;
    const nombre = document.getElementById('nombreEmpleado').value.trim();
    const apellido = document.getElementById('apellidoEmpleado').value.trim();
    const legajo = document.getElementById('legajoEmpleado').value.trim();

    if (!sucursalId || !nombre || !apellido || !legajo) return alert('Completá todos los campos del empleado.');
    
    if (!esSoloNumeros(legajo)) return alert('Error: El legajo debe ser un número entero.');

    empleados.push({ id: Date.now(), sucursalId: Number(sucursalId), nombre, apellido, legajo });
    actualizarUIEmpleados();
    formEmpleado.reset();
});

function actualizarUIEmpleados() {
    if (empleados.length === 0) {
        tablaEmpleados.innerHTML = '<tr><td colspan="4" class="text-center text-muted py-3">No hay empleados registrados.</td></tr>';
    } else {
        tablaEmpleados.innerHTML = empleados.map(empl => {
            const sucursalLugar = sucursales.find(suc => suc.id === empl.sucursalId);
            const dirSucursal = sucursalLugar ? sucursalLugar.direccion : 'Sucursal eliminada';

            return `
            <tr>
                <td class="align-middle">${empl.legajo}</td>
                <td class="align-middle">${empl.apellido}, ${empl.nombre}</td>
                <td class="align-middle">${dirSucursal}</td>
                <td class="text-end">
                    <button class="btn btn-sm btn-warning me-1" onclick="abrirModalEmpleado(${empl.id})">Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarEmpleado(${empl.id})">Eliminar</button>
                </td>
            </tr>`;
        }).join('');
    }
}

function eliminarEmpleado(id) {
    empleados = empleados.filter(emp => emp.id !== id);
    actualizarUIEmpleados();
}

// Edición de Empleado
function abrirModalEmpleado(id) {
    idEdicionEmpleado = id;
    const emp = empleados.find(e => e.id === id);
    
    const selectEdit = document.getElementById('editSelectSucursal');
    selectEdit.innerHTML = sucursales.map(suc => {
        const empresaDuena = empresas.find(e => e.id === suc.empresaId);
        return `<option value="${suc.id}">${suc.direccion} (${empresaDuena ? empresaDuena.razonSocial : ''})</option>`;
    }).join('');
    selectEdit.value = emp.sucursalId;
    
    document.getElementById('editNombreEmpleado').value = emp.nombre;
    document.getElementById('editApellidoEmpleado').value = emp.apellido;
    document.getElementById('editLegajoEmpleado').value = emp.legajo;
    
    if(!modalEmpleado) modalEmpleado = new bootstrap.Modal(document.getElementById('modalEditarEmpleado'));
    modalEmpleado.show();
}

function guardarEdicionEmpleado() {
    const sucursalId = document.getElementById('editSelectSucursal').value;
    const nombre = document.getElementById('editNombreEmpleado').value.trim();
    const apellido = document.getElementById('editApellidoEmpleado').value.trim();
    const legajo = document.getElementById('editLegajoEmpleado').value.trim();

    if (!sucursalId || !nombre || !apellido || !legajo) return alert('Completá todos los campos.');
    
    // NUEVA VALIDACIÓN
    if (!esSoloNumeros(legajo)) return alert('Error: El legajo debe ser un número entero.');

    const emp = empleados.find(e => e.id === idEdicionEmpleado);
    emp.sucursalId = Number(sucursalId);
    emp.nombre = nombre;
    emp.apellido = apellido;
    emp.legajo = legajo;

    modalEmpleado.hide();
    actualizarUIEmpleados();
}