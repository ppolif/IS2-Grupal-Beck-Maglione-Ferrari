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
function renderPersonaRow(p) {
    const dom = p.domicilio ? `${p.domicilio.calle || ''} ${p.domicilio.numero || ''}`.trim() : '-';
    const loc = (p.domicilio && p.domicilio.localidad) ? p.domicilio.localidad.denominacion : '-';

    let mapaHtml = '<span class="text-muted small fst-italic">Sin ubicación</span>';
    if (p.domicilio && p.domicilio.latitud && p.domicilio.longitud) {
        const lat = p.domicilio.latitud.trim();
        const lng = p.domicilio.longitud.trim();
        if (lat && lng) {
            const mapUrl = `https://www.google.com/maps?q=${encodeURIComponent(lat)},${encodeURIComponent(lng)}`;
            mapaHtml = `
                <a href="${mapUrl}" target="_blank" rel="noopener noreferrer" class="btn btn-sm btn-outline-success d-inline-flex align-items-center gap-1" title="Ver en Google Maps (${lat}, ${lng})">
                    <i class="bi bi-geo-alt-fill text-danger"></i> <span>Ver Mapa</span>
                </a>
            `;
        }
    }

    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td class="fw-bold">${p.id}</td>
        <td>${p.nombre || ''} ${p.apellido || ''}</td>
        <td><span class="badge bg-light text-dark border">${p.dni}</span></td>
        <td>${dom || '-'}</td>
        <td><span class="badge bg-secondary-subtle text-secondary">${loc}</span></td>
        <td>${mapaHtml}</td>
        <td class="text-center">
            <button class="btn btn-sm btn-outline-primary me-1" onclick="editarPersona(${p.id})" title="Editar Persona">
                <i class="bi bi-pencil"></i>
            </button>
            <button class="btn btn-sm btn-outline-danger" onclick="eliminarPersona(${p.id})" title="Eliminar Persona">
                <i class="bi bi-trash"></i>
            </button>
        </td>
    `;
    return tr;
}

async function cargarPersonas() {
    try {
        const res = await fetch(API_URL.PERSONAS);
        const data = await res.json();
        const tbody = document.getElementById('personasTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No hay personas registradas</td></tr>';
            return;
        }

        data.forEach(p => {
            tbody.appendChild(renderPersonaRow(p));
        });
    } catch (err) {
        console.error(err);
        document.getElementById('personasTableBody').innerHTML = '<tr><td colspan="7" class="text-center text-danger py-4">Error al cargar personas</td></tr>';
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
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Sin resultados</td></tr>';
            return;
        }

        data.forEach(p => {
            tbody.appendChild(renderPersonaRow(p));
        });
    } catch (err) {
        notificar('Error en la búsqueda de personas', 'danger');
    }
}

async function poblarSelectLocalidades(selectedId = null) {
    const select = document.getElementById('personaLocalidadSelect');
    select.innerHTML = '<option value="">Seleccione Localidad...</option>';
    try {
        const res = await fetch(API_URL.LOCALIDADES);
        const locs = await res.json();
        locs.forEach(l => {
            const opt = document.createElement('option');
            opt.value = l.id;
            opt.textContent = l.denominacion;
            if (selectedId && Number(selectedId) === Number(l.id)) {
                opt.selected = true;
            }
            select.appendChild(opt);
        });
    } catch (err) {
        console.error('Error cargando localidades para select', err);
    }
}

async function abrirModalPersona() {
    document.getElementById('personaForm').reset();
    document.getElementById('personaId').value = '';
    if (document.getElementById('personaLatitud')) document.getElementById('personaLatitud').value = '';
    if (document.getElementById('personaLongitud')) document.getElementById('personaLongitud').value = '';
    document.getElementById('personaModalTitle').textContent = 'Nueva Persona';
    await poblarSelectLocalidades();
    personaModal.show();
}

async function editarPersona(id) {
    try {
        const res = await fetch(`${API_URL.PERSONAS}/${id}`);
        if (!res.ok) throw new Error('No se pudo obtener la persona');
        const p = await res.json();

        document.getElementById('personaForm').reset();
        document.getElementById('personaId').value = p.id;
        document.getElementById('personaNombre').value = p.nombre || '';
        document.getElementById('personaApellido').value = p.apellido || '';
        document.getElementById('personaDni').value = p.dni || '';
        document.getElementById('personaCalle').value = p.domicilio ? p.domicilio.calle || '' : '';
        document.getElementById('personaNumero').value = p.domicilio ? p.domicilio.numero || '' : '';
        if (document.getElementById('personaLatitud')) {
            document.getElementById('personaLatitud').value = (p.domicilio && p.domicilio.latitud) ? p.domicilio.latitud : '';
        }
        if (document.getElementById('personaLongitud')) {
            document.getElementById('personaLongitud').value = (p.domicilio && p.domicilio.longitud) ? p.domicilio.longitud : '';
        }
        document.getElementById('personaModalTitle').textContent = 'Editar Persona';

        const locId = (p.domicilio && p.domicilio.localidad) ? p.domicilio.localidad.id : null;
        await poblarSelectLocalidades(locId);

        personaModal.show();
    } catch (err) {
        notificar('Error al cargar datos para edición', 'danger');
    }
}

async function guardarPersona(e) {
    e.preventDefault();
    const id = document.getElementById('personaId').value;
    const locId = document.getElementById('personaLocalidadSelect').value;

    const latInput = document.getElementById('personaLatitud');
    const lngInput = document.getElementById('personaLongitud');

    const dto = {
        nombre: document.getElementById('personaNombre').value.trim(),
        apellido: document.getElementById('personaApellido').value.trim(),
        dni: parseInt(document.getElementById('personaDni').value) || 0,
        domicilio: {
            calle: document.getElementById('personaCalle').value.trim(),
            numero: parseInt(document.getElementById('personaNumero').value) || 0,
            latitud: latInput ? latInput.value.trim() : '',
            longitud: lngInput ? lngInput.value.trim() : '',
            localidad: locId ? { id: parseInt(locId) } : null
        },
        libros: []
    };

    try {
        const url = id ? `${API_URL.PERSONAS}/${id}` : API_URL.PERSONAS;
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (!res.ok) throw new Error();
        personaModal.hide();
        notificar(`Persona ${id ? 'actualizada' : 'guardada'} correctamente`);
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
   LIBROS (1.c BASE)
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
            const vencimientoBadge = libro.fechaVencimientoDevolucion 
                ? `<span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">${libro.fechaVencimientoDevolucion}</span>`
                : '<span class="text-muted small">-</span>';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${libro.id}</td>
                <td class="fw-semibold text-primary">${escapeQuotes(libro.titulo)}</td>
                <td><span class="badge bg-light text-dark border">${libro.genero || '-'}</span></td>
                <td>${libro.fecha || '-'}</td>
                <td>${libro.paginas || '-'}</td>
                <td>${vencimientoBadge}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarLibro(${libro.id}, '${escapeQuotes(libro.titulo)}', '${escapeQuotes(libro.genero)}', ${libro.fecha || 0}, ${libro.paginas || 0}, '${libro.fechaVencimientoDevolucion || ''}')" title="Editar Libro">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarLibro(${libro.id})" title="Eliminar Libro">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        document.getElementById('librosTableBody').innerHTML = 
            '<tr><td colspan="7" class="text-center py-4 text-muted"><i class="bi bi-info-circle me-1"></i> Error al conectar con el servicio de libros.</td></tr>';
    }
}

async function buscarLibros() {
    const inputFiltro = document.getElementById('libroFiltro');
    const filtro = inputFiltro ? inputFiltro.value.trim() : '';
    if (!filtro) return cargarLibros();

    try {
        const res = await fetch(`${API_URL.LIBROS}/search?filtro=${encodeURIComponent(filtro)}`);
        const data = await res.json();
        const tbody = document.getElementById('librosTableBody');
        tbody.innerHTML = '';

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">Sin coincidencias para "' + escapeQuotes(filtro) + '"</td></tr>';
            return;
        }

        data.forEach(libro => {
            const vencimientoBadge = libro.fechaVencimientoDevolucion 
                ? `<span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">${libro.fechaVencimientoDevolucion}</span>`
                : '<span class="text-muted small">-</span>';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${libro.id}</td>
                <td class="fw-semibold text-primary">${escapeQuotes(libro.titulo)}</td>
                <td><span class="badge bg-light text-dark border">${libro.genero || '-'}</span></td>
                <td>${libro.fecha || '-'}</td>
                <td>${libro.paginas || '-'}</td>
                <td>${vencimientoBadge}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarLibro(${libro.id}, '${escapeQuotes(libro.titulo)}', '${escapeQuotes(libro.genero)}', ${libro.fecha || 0}, ${libro.paginas || 0}, '${libro.fechaVencimientoDevolucion || ''}')" title="Editar Libro">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarLibro(${libro.id})" title="Eliminar Libro">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        notificar('Error en la búsqueda de libros', 'danger');
    }
}

function abrirModalLibro() {
    document.getElementById('libroForm').reset();
    document.getElementById('libroId').value = '';
    document.getElementById('libroFechaVencimiento').value = '';
    document.getElementById('libroModalTitle').textContent = 'Nuevo Libro';
    libroModal.show();
}

function editarLibro(id, titulo, genero, fecha, paginas, fechaVencimiento = '') {
    document.getElementById('libroId').value = id;
    document.getElementById('libroTitulo').value = titulo;
    document.getElementById('libroGenero').value = genero || '';
    document.getElementById('libroFecha').value = fecha || '';
    document.getElementById('libroPaginas').value = paginas || '';
    document.getElementById('libroFechaVencimiento').value = fechaVencimiento || '';
    document.getElementById('libroModalTitle').textContent = 'Editar Libro';
    libroModal.show();
}

async function guardarLibro(e) {
    e.preventDefault();
    const id = document.getElementById('libroId').value;
    const fechaVencVal = document.getElementById('libroFechaVencimiento').value;

    const dto = {
        titulo: document.getElementById('libroTitulo').value.trim(),
        genero: document.getElementById('libroGenero').value.trim(),
        fecha: parseInt(document.getElementById('libroFecha').value) || 0,
        paginas: parseInt(document.getElementById('libroPaginas').value) || 0,
        fechaVencimientoDevolucion: fechaVencVal || null
    };

    try {
        const url = id ? `${API_URL.LIBROS}/${id}` : API_URL.LIBROS;
        const res = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (!res.ok) throw new Error('Error al registrar libro');
        libroModal.hide();
        notificar(`Libro ${id ? 'actualizado' : 'guardado'} correctamente`);
        cargarLibros();
    } catch (err) {
        notificar('Error al guardar libro', 'danger');
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
function escapeQuotes(str) {
    if (!str) return '';
    return String(str).replace(/'/g, "\\'").replace(/"/g, '&quot;');
}
