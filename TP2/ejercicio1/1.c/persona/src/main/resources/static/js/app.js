// API Endpoints Base URL
const API_URL = {
    PERSONAS: '/api/v1/persona',
    AUTORES: '/api/v1/autores',
    LOCALIDADES: '/api/v1/localidades',
    LIBROS: '/api/v1/libros'
};

// Bootstrap Modal Instances
let personaModal, libroModal, autorModal, localidadModal, liveToast;

document.addEventListener('DOMContentLoaded', () => {
    personaModal = new bootstrap.Modal(document.getElementById('personaModal'));
    libroModal = new bootstrap.Modal(document.getElementById('libroModal'));
    autorModal = new bootstrap.Modal(document.getElementById('autorModal'));
    localidadModal = new bootstrap.Modal(document.getElementById('localidadModal'));
    liveToast = new bootstrap.Toast(document.getElementById('liveToast'));

    cargarPersonas();
    cargarAutores();
    cargarLocalidades();
    cargarLibros();
});

// Toast Helper
function notificar(mensaje, tipo = 'success') {
    const toastEl = document.getElementById('liveToast');
    const toastMsg = document.getElementById('toastMessage');
    toastEl.className = `toast align-items-center text-white border-0 bg-${tipo}`;
    toastMsg.textContent = mensaje;
    liveToast.show();
}

/* ==========================================================================
   LOCALIDADES
   ========================================================================== */
async function cargarLocalidades() {
    try {
        const res = await fetch(API_URL.LOCALIDADES);
        const data = await res.json();
        const tbody = document.getElementById('localidadesTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center py-4 text-muted">No hay localidades registradas</td></tr>';
            return;
        }

        data.forEach(loc => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${loc.id}</td>
                <td>${loc.denominacion || '-'}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarLocalidad(${loc.id}, '${escapeQuotes(loc.denominacion)}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarLocalidad(${loc.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error(err);
        document.getElementById('localidadesTableBody').innerHTML = '<tr><td colspan="3" class="text-center text-danger py-4">Error al cargar localidades</td></tr>';
    }
}

async function buscarLocalidades() {
    const filtro = document.getElementById('localidadFiltro').value.trim();
    if (!filtro) return cargarLocalidades();

    try {
        const res = await fetch(`${API_URL.LOCALIDADES}/search?filtro=${encodeURIComponent(filtro)}`);
        const data = await res.json();
        const tbody = document.getElementById('localidadesTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="3" class="text-center py-4 text-muted">Sin coincidencias</td></tr>';
            return;
        }

        data.forEach(loc => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${loc.id}</td>
                <td>${loc.denominacion || '-'}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarLocalidad(${loc.id}, '${escapeQuotes(loc.denominacion)}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarLocalidad(${loc.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        notificar('Error en la búsqueda de localidades', 'danger');
    }
}

function abrirModalLocalidad() {
    document.getElementById('localidadForm').reset();
    document.getElementById('localidadId').value = '';
    document.getElementById('localidadModalTitle').textContent = 'Nueva Localidad';
    localidadModal.show();
}

function editarLocalidad(id, denominacion) {
    document.getElementById('localidadId').value = id;
    document.getElementById('localidadDenominacion').value = denominacion;
    document.getElementById('localidadModalTitle').textContent = 'Editar Localidad';
    localidadModal.show();
}

async function guardarLocalidad(e) {
    e.preventDefault();
    const id = document.getElementById('localidadId').value;
    const denominacion = document.getElementById('localidadDenominacion').value;
    const dto = { denominacion };

    try {
        const url = id ? `${API_URL.LOCALIDADES}/${id}` : API_URL.LOCALIDADES;
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (!res.ok) throw new Error('Error en el servidor');
        localidadModal.hide();
        notificar(`Localidad ${id ? 'actualizada' : 'creada'} correctamente`);
        cargarLocalidades();
    } catch (err) {
        notificar('Error al guardar localidad', 'danger');
    }
}

async function eliminarLocalidad(id) {
    if (!confirm(`¿Desea eliminar la localidad #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.LOCALIDADES}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Localidad eliminada');
        cargarLocalidades();
    } catch (err) {
        notificar('Error al eliminar localidad', 'danger');
    }
}

/* ==========================================================================
   AUTORES
   ========================================================================== */
async function cargarAutores() {
    try {
        const res = await fetch(API_URL.AUTORES);
        const data = await res.json();
        const tbody = document.getElementById('autoresTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">No hay autores registrados</td></tr>';
            return;
        }

        data.forEach(autor => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${autor.id}</td>
                <td>${autor.nombre || '-'}</td>
                <td>${autor.apellido || '-'}</td>
                <td><small class="text-muted">${autor.biografia ? autor.biografia.substring(0, 70) + '...' : '-'}</small></td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarAutor(${autor.id}, '${escapeQuotes(autor.nombre)}', '${escapeQuotes(autor.apellido)}', '${escapeQuotes(autor.biografia)}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarAutor(${autor.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error(err);
        document.getElementById('autoresTableBody').innerHTML = '<tr><td colspan="5" class="text-center text-danger py-4">Error al cargar autores</td></tr>';
    }
}

async function buscarAutores() {
    const filtro = document.getElementById('autorFiltro').value.trim();
    if (!filtro) return cargarAutores();

    try {
        const res = await fetch(`${API_URL.AUTORES}/search?filtro=${encodeURIComponent(filtro)}`);
        const data = await res.json();
        const tbody = document.getElementById('autoresTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center py-4 text-muted">Sin coincidencias</td></tr>';
            return;
        }

        data.forEach(autor => {
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${autor.id}</td>
                <td>${autor.nombre || '-'}</td>
                <td>${autor.apellido || '-'}</td>
                <td><small class="text-muted">${autor.biografia ? autor.biografia.substring(0, 70) + '...' : '-'}</small></td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarAutor(${autor.id}, '${escapeQuotes(autor.nombre)}', '${escapeQuotes(autor.apellido)}', '${escapeQuotes(autor.biografia)}')">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarAutor(${autor.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        notificar('Error en la búsqueda de autores', 'danger');
    }
}

function abrirModalAutor() {
    document.getElementById('autorForm').reset();
    document.getElementById('autorId').value = '';
    document.getElementById('autorModalTitle').textContent = 'Nuevo Autor';
    autorModal.show();
}

function editarAutor(id, nombre, apellido, biografia) {
    document.getElementById('autorId').value = id;
    document.getElementById('autorNombre').value = nombre;
    document.getElementById('autorApellido').value = apellido;
    document.getElementById('autorBiografia').value = biografia || '';
    document.getElementById('autorModalTitle').textContent = 'Editar Autor';
    autorModal.show();
}

async function guardarAutor(e) {
    e.preventDefault();
    const id = document.getElementById('autorId').value;
    const dto = {
        nombre: document.getElementById('autorNombre').value,
        apellido: document.getElementById('autorApellido').value,
        biografia: document.getElementById('autorBiografia').value
    };

    try {
        const url = id ? `${API_URL.AUTORES}/${id}` : API_URL.AUTORES;
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (!res.ok) throw new Error();
        autorModal.hide();
        notificar(`Autor ${id ? 'actualizado' : 'creado'} correctamente`);
        cargarAutores();
    } catch (err) {
        notificar('Error al guardar autor', 'danger');
    }
}

async function eliminarAutor(id) {
    if (!confirm(`¿Desea eliminar el autor #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.AUTORES}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Autor eliminado');
        cargarAutores();
    } catch (err) {
        notificar('Error al eliminar autor', 'danger');
    }
}

/* ==========================================================================
   PERSONAS
   ========================================================================== */
async function cargarPersonas() {
    try {
        const res = await fetch(API_URL.PERSONAS);
        const data = await res.json();
        const tbody = document.getElementById('personasTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">No hay personas registradas</td></tr>';
            return;
        }

        data.forEach(p => {
            const dom = p.domicilio ? `${p.domicilio.calle || ''} ${p.domicilio.numero || ''}`.trim() : '-';
            const loc = (p.domicilio && p.domicilio.localidad) ? p.domicilio.localidad.denominacion : '-';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${p.id}</td>
                <td>${p.nombre || ''} ${p.apellido || ''}</td>
                <td><span class="badge bg-light text-dark border">${p.dni}</span></td>
                <td>${dom || '-'}</td>
                <td><span class="badge bg-secondary-subtle text-secondary">${loc}</span></td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarPersona(${p.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error(err);
        document.getElementById('personasTableBody').innerHTML = '<tr><td colspan="6" class="text-center text-danger py-4">Error al cargar personas</td></tr>';
    }
}

async function buscarPersonas() {
    const filtro = document.getElementById('personaFiltro').value.trim();
    if (!filtro) return cargarPersonas();

    try {
        const res = await fetch(`${API_URL.PERSONAS}/search?filtro=${encodeURIComponent(filtro)}`);
        const data = await res.json();
        const tbody = document.getElementById('personasTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-muted">Sin resultados</td></tr>';
            return;
        }

        data.forEach(p => {
            const dom = p.domicilio ? `${p.domicilio.calle || ''} ${p.domicilio.numero || ''}`.trim() : '-';
            const loc = (p.domicilio && p.domicilio.localidad) ? p.domicilio.localidad.denominacion : '-';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${p.id}</td>
                <td>${p.nombre || ''} ${p.apellido || ''}</td>
                <td><span class="badge bg-light text-dark border">${p.dni}</span></td>
                <td>${dom || '-'}</td>
                <td><span class="badge bg-secondary-subtle text-secondary">${loc}</span></td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarPersona(${p.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        notificar('Error en la búsqueda de personas', 'danger');
    }
}

async function abrirModalPersona() {
    document.getElementById('personaForm').reset();
    document.getElementById('personaId').value = '';

    // Poblar combo de localidades
    const select = document.getElementById('personaLocalidadSelect');
    select.innerHTML = '<option value="">Seleccione Localidad...</option>';
    try {
        const res = await fetch(API_URL.LOCALIDADES);
        const locs = await res.json();
        locs.forEach(l => {
            const opt = document.createElement('option');
            opt.value = l.id;
            opt.textContent = l.denominacion;
            select.appendChild(opt);
        });
    } catch (err) {
        console.error('Error cargando localidades para select', err);
    }

    personaModal.show();
}

async function guardarPersona(e) {
    e.preventDefault();
    const locId = document.getElementById('personaLocalidadSelect').value;

    const dto = {
        nombre: document.getElementById('personaNombre').value,
        apellido: document.getElementById('personaApellido').value,
        dni: parseInt(document.getElementById('personaDni').value) || 0,
        domicilio: {
            calle: document.getElementById('personaCalle').value,
            numero: parseInt(document.getElementById('personaNumero').value) || 0,
            localidad: locId ? { id: parseInt(locId) } : null
        },
        libros: []
    };

    try {
        const res = await fetch(API_URL.PERSONAS, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (!res.ok) throw new Error();
        personaModal.hide();
        notificar('Persona guardada correctamente');
        cargarPersonas();
    } catch (err) {
        notificar('Error al guardar persona', 'danger');
    }
}

async function eliminarPersona(id) {
    if (!confirm(`¿Desea eliminar a la persona #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.PERSONAS}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Persona eliminada');
        cargarPersonas();
    } catch (err) {
        notificar('Error al eliminar persona', 'danger');
    }
}

/* ==========================================================================
   LIBROS & PDF (APERTURA EN NUEVA SOLAPA CON target="_blank")
   ========================================================================== */
async function cargarLibros() {
    try {
        const res = await fetch(API_URL.LIBROS);
        const tbody = document.getElementById('librosTableBody');
        tbody.innerHTML = '';

        if (!res.ok) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Aún no hay endpoint activo de Libros o no hay registros cargados.</td></tr>';
            return;
        }

        const data = await res.json();
        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No hay libros registrados</td></tr>';
            return;
        }

        data.forEach(libro => {
            const pdfName = libro.archivoPdfNombre || `libro_${sanitizarNombre(libro.titulo)}_.pdf`;
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${libro.id}</td>
                <td class="fw-semibold text-primary">${libro.titulo}</td>
                <td><span class="badge bg-light text-dark border">${libro.genero || '-'}</span></td>
                <td>${libro.fecha || '-'}</td>
                <td>${libro.paginas || '-'}</td>
                <td>
                    <span class="badge-pdf d-inline-flex align-items-center gap-1">
                        <i class="bi bi-file-earmark-pdf"></i> ${pdfName}
                    </span>
                </td>
                <td class="text-center">
                    <button class="btn btn-open-pdf d-inline-flex align-items-center gap-1 shadow-sm" 
                            onclick="abrirPdfLibro(${libro.id})" title="Abrir PDF en pestaña nueva">
                        <i class="bi bi-box-arrow-up-right"></i> Abrir PDF
                    </button>
                    <button class="btn btn-sm btn-outline-danger ms-1" onclick="eliminarLibro(${libro.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        document.getElementById('librosTableBody').innerHTML = 
            '<tr><td colspan="7" class="text-center py-4 text-muted"><i class="bi bi-info-circle me-1"></i> El módulo de Libros estará disponible tan pronto se ejecute el backend.</td></tr>';
    }
}

function abrirModalLibro() {
    document.getElementById('libroForm').reset();
    document.getElementById('libroId').value = '';
    libroModal.show();
}

/**
 * Abre el PDF del libro en una pestaña/solapa nueva del navegador
 */
function abrirPdfLibro(id) {
    const pdfUrl = `${API_URL.LIBROS}/${id}/pdf`;
    window.open(pdfUrl, '_blank');
}

async function guardarLibro(e) {
    e.preventDefault();
    const titulo = document.getElementById('libroTitulo').value;
    const genero = document.getElementById('libroGenero').value;
    const fecha = parseInt(document.getElementById('libroFecha').value) || 0;
    const paginas = parseInt(document.getElementById('libroPaginas').value) || 0;
    const fileInput = document.getElementById('libroArchivoPdf');

    const formData = new FormData();
    formData.append('titulo', titulo);
    formData.append('genero', genero);
    formData.append('fecha', fecha);
    formData.append('paginas', paginas);

    if (fileInput.files.length > 0) {
        formData.append('file', fileInput.files[0]);
    }

    try {
        const res = await fetch(API_URL.LIBROS, {
            method: 'POST',
            body: formData
        });
        if (!res.ok) throw new Error('Error al registrar libro');
        libroModal.hide();
        notificar('Libro cargado con éxito');
        cargarLibros();
    } catch (err) {
        notificar('El backend de carga de libros aún no ha procesado la petición', 'warning');
    }
}

async function eliminarLibro(id) {
    if (!confirm(`¿Desea eliminar el libro #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.LIBROS}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Libro eliminado');
        cargarLibros();
    } catch (err) {
        notificar('Error al eliminar libro', 'danger');
    }
}

// Utilitarios
function sanitizarNombre(nombre) {
    return (nombre || 'libro').trim().toLowerCase().replace(/[^a-z0-9_-]/gi, '_');
}

function escapeQuotes(str) {
    if (!str) return '';
    return str.replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
