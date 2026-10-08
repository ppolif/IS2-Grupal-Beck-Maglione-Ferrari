// Configuración de URL base dinámica (soporta http://localhost:9000, Live Server, IntelliJ y file://)
const API_BASE = (window.location.protocol === 'http:' || window.location.protocol === 'https:') && window.location.port === '9000'
    ? ''
    : 'http://localhost:9000';

const API_URL = {
    PERSONAS: `${API_BASE}/api/v1/persona`,
    AUTORES: `${API_BASE}/api/v1/autores`,
    LOCALIDADES: `${API_BASE}/api/v1/localidades`,
    LIBROS: `${API_BASE}/api/v1/libros`,
    PRESTAMOS: `${API_BASE}/api/v1/prestamos`,
    REPORTES: `${API_BASE}/api/v1/reportes`,
    MIGRACION: `${API_BASE}/api/v1/migracion`
};

// Bootstrap Modal Instances
let personaModal, libroModal, autorModal, localidadModal, prestamoModal, liveToast;

document.addEventListener('DOMContentLoaded', () => {
    personaModal = new bootstrap.Modal(document.getElementById('personaModal'));
    libroModal = new bootstrap.Modal(document.getElementById('libroModal'));
    autorModal = new bootstrap.Modal(document.getElementById('autorModal'));
    localidadModal = new bootstrap.Modal(document.getElementById('localidadModal'));
    prestamoModal = new bootstrap.Modal(document.getElementById('prestamoModal'));
    liveToast = new bootstrap.Toast(document.getElementById('liveToast'));

    cargarPersonas();
    cargarAutores();
    cargarLocalidades();
    cargarLibros();
    cargarPrestamos();
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
    if (!confirm(`¿Desea dar de baja la localidad #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.LOCALIDADES}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Localidad dada de baja correctamente');
        cargarLocalidades();
    } catch (err) {
        notificar('Error al dar de baja localidad', 'danger');
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
    if (!confirm(`¿Desea dar de baja al autor #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.AUTORES}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Autor dado de baja correctamente');
        cargarAutores();
    } catch (err) {
        notificar('Error al dar de baja autor', 'danger');
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
            tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4 text-muted">No hay personas registradas</td></tr>';
            return;
        }

        data.forEach(p => {
            const dom = p.domicilio ? `${p.domicilio.calle || ''} ${p.domicilio.numero || ''}`.trim() : '-';
            const loc = (p.domicilio && p.domicilio.localidad) ? p.domicilio.localidad.denominacion : '-';
            const tienePrestamo = Boolean(p.tienePrestamo);
            const prestamoBadge = tienePrestamo
                ? `<span class="badge bg-warning text-dark"><i class="bi bi-bookmark-check-fill me-1"></i>Tiene Préstamo</span>`
                : `<span class="badge bg-secondary-subtle text-secondary">Sin Préstamo</span>`;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${p.id}</td>
                <td>${p.nombre || ''} ${p.apellido || ''}</td>
                <td><span class="badge bg-light text-dark border">${p.dni}</span></td>
                <td>${dom || '-'}</td>
                <td><span class="badge bg-secondary-subtle text-secondary">${loc}</span></td>
                <td>${prestamoBadge}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarPersona(${p.id})" title="Editar Persona">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarPersona(${p.id})" title="Eliminar Persona">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
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
            const dom = p.domicilio ? `${p.domicilio.calle || ''} ${p.domicilio.numero || ''}`.trim() : '-';
            const loc = (p.domicilio && p.domicilio.localidad) ? p.domicilio.localidad.denominacion : '-';
            const tienePrestamo = Boolean(p.tienePrestamo);
            const prestamoBadge = tienePrestamo
                ? `<span class="badge bg-warning text-dark"><i class="bi bi-bookmark-check-fill me-1"></i>Tiene Préstamo</span>`
                : `<span class="badge bg-secondary-subtle text-secondary">Sin Préstamo</span>`;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${p.id}</td>
                <td>${p.nombre || ''} ${p.apellido || ''}</td>
                <td><span class="badge bg-light text-dark border">${p.dni}</span></td>
                <td>${dom || '-'}</td>
                <td><span class="badge bg-secondary-subtle text-secondary">${loc}</span></td>
                <td>${prestamoBadge}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarPersona(${p.id})" title="Editar Persona">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarPersona(${p.id})" title="Eliminar Persona">
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

    const dto = {
        nombre: document.getElementById('personaNombre').value.trim(),
        apellido: document.getElementById('personaApellido').value.trim(),
        dni: parseInt(document.getElementById('personaDni').value) || 0,
        domicilio: {
            calle: document.getElementById('personaCalle').value.trim(),
            numero: parseInt(document.getElementById('personaNumero').value) || 0,
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
    if (!confirm(`¿Desea dar de baja a la persona #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.PERSONAS}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Persona dada de baja correctamente');
        cargarPersonas();
    } catch (err) {
        notificar('Error al dar de baja persona', 'danger');
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
            const estaPrestado = Boolean(libro.prestado);
            const badgeDisponibilidad = estaPrestado
                ? '<span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>Prestado</span>'
                : '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Disponible</span>';

            const botonPrestar = estaPrestado
                ? '<button class="btn btn-sm btn-outline-secondary me-1" disabled title="Libro no disponible (ya prestado)"><i class="bi bi-journal-x"></i></button>'
                : `<button class="btn btn-sm btn-outline-success me-1" onclick="abrirModalPrestamoConLibro(${libro.id})" title="Prestar Libro"><i class="bi bi-journal-arrow-up"></i></button>`;

            const tr = document.createElement('tr');
            if (estaPrestado) tr.classList.add('table-light');
            tr.innerHTML = `
                <td class="fw-bold">${libro.id}</td>
                <td class="fw-semibold text-primary">${escapeQuotes(libro.titulo)}</td>
                <td><span class="badge bg-light text-dark border">${libro.genero || '-'}</span></td>
                <td>${libro.fecha || '-'}</td>
                <td>${libro.paginas || '-'}</td>
                <td>${badgeDisponibilidad}</td>
                <td class="text-center">
                    ${botonPrestar}
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarLibro(${libro.id}, '${escapeQuotes(libro.titulo)}', '${escapeQuotes(libro.genero)}', ${libro.fecha || 0}, ${libro.paginas || 0})" title="Editar Libro">
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
            const estaPrestado = Boolean(libro.prestado);
            const badgeDisponibilidad = estaPrestado
                ? '<span class="badge bg-danger"><i class="bi bi-x-circle me-1"></i>Prestado</span>'
                : '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Disponible</span>';

            const botonPrestar = estaPrestado
                ? '<button class="btn btn-sm btn-outline-secondary me-1" disabled title="Libro no disponible (ya prestado)"><i class="bi bi-journal-x"></i></button>'
                : `<button class="btn btn-sm btn-outline-success me-1" onclick="abrirModalPrestamoConLibro(${libro.id})" title="Prestar Libro"><i class="bi bi-journal-arrow-up"></i></button>`;

            const tr = document.createElement('tr');
            if (estaPrestado) tr.classList.add('table-light');
            tr.innerHTML = `
                <td class="fw-bold">${libro.id}</td>
                <td class="fw-semibold text-primary">${escapeQuotes(libro.titulo)}</td>
                <td><span class="badge bg-light text-dark border">${libro.genero || '-'}</span></td>
                <td>${libro.fecha || '-'}</td>
                <td>${libro.paginas || '-'}</td>
                <td>${badgeDisponibilidad}</td>
                <td class="text-center">
                    ${botonPrestar}
                    <button class="btn btn-sm btn-outline-primary me-1" onclick="editarLibro(${libro.id}, '${escapeQuotes(libro.titulo)}', '${escapeQuotes(libro.genero)}', ${libro.fecha || 0}, ${libro.paginas || 0})" title="Editar Libro">
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
    document.getElementById('libroModalTitle').textContent = 'Nuevo Libro';
    libroModal.show();
}

function editarLibro(id, titulo, genero, fecha, paginas) {
    document.getElementById('libroId').value = id;
    document.getElementById('libroTitulo').value = titulo;
    document.getElementById('libroGenero').value = genero || '';
    document.getElementById('libroFecha').value = fecha || '';
    document.getElementById('libroPaginas').value = paginas || '';
    document.getElementById('libroModalTitle').textContent = 'Editar Libro';
    libroModal.show();
}

async function guardarLibro(e) {
    e.preventDefault();
    const id = document.getElementById('libroId').value;
    const dto = {
        titulo: document.getElementById('libroTitulo').value.trim(),
        genero: document.getElementById('libroGenero').value.trim(),
        fecha: parseInt(document.getElementById('libroFecha').value) || 0,
        paginas: parseInt(document.getElementById('libroPaginas').value) || 0
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
    if (!confirm(`¿Desea dar de baja el libro #${id}?`)) return;
    try {
        const res = await fetch(`${API_URL.LIBROS}/${id}`, { method: 'DELETE' });
        if (!res.ok) throw new Error();
        notificar('Libro dado de baja correctamente');
        cargarLibros();
    } catch (err) {
        notificar('Error al dar de baja libro', 'danger');
    }
}

/* ==========================================================================
   PRÉSTAMOS
   ========================================================================== */
async function cargarPrestamos() {
    try {
        const res = await fetch(API_URL.PRESTAMOS);
        const tbody = document.getElementById('prestamosTableBody');
        tbody.innerHTML = '';

        if (!res.ok) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">Aún no hay préstamos registrados.</td></tr>';
            return;
        }

        const data = await res.json();
        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No hay préstamos registrados</td></tr>';
            return;
        }

        data.forEach(p => {
            const esActivo = p.estado === 'ACTIVO';
            const estadoBadge = esActivo
                ? '<span class="badge bg-success"><i class="bi bi-clock-history me-1"></i>Activo</span>'
                : '<span class="badge bg-secondary"><i class="bi bi-check2-all me-1"></i>Devuelto</span>';

            const nombrePersona = p.persona ? `${p.persona.nombre || ''} ${p.persona.apellido || ''}`.trim() : '-';
            const dniPersona = p.persona ? p.persona.dni : '-';
            const tituloLibro = p.libro ? p.libro.titulo : '-';

            const botonDevolver = esActivo
                ? `<button class="btn btn-sm btn-outline-warning me-1" onclick="devolverPrestamo(${p.id})" title="Registrar Devolución"><i class="bi bi-arrow-return-left"></i> Devolver</button>`
                : '<span class="text-muted small me-2"><i class="bi bi-check-circle text-success"></i> Devuelto</span>';

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td class="fw-bold">${p.id}</td>
                <td>${p.fechaPrestamo || '-'}</td>
                <td class="fw-semibold">${escapeQuotes(nombrePersona)}</td>
                <td><span class="badge bg-light text-dark border">${dniPersona}</span></td>
                <td class="text-primary fw-medium">${escapeQuotes(tituloLibro)}</td>
                <td>${estadoBadge}</td>
                <td>${p.fechaDevolucion || '-'}</td>
                <td class="text-center text-nowrap">
                    ${botonDevolver}
                    <button class="btn btn-sm btn-outline-danger" onclick="eliminarPrestamo(${p.id})" title="Eliminar registro">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (err) {
        console.error('Error cargando préstamos', err);
        document.getElementById('prestamosTableBody').innerHTML = 
            '<tr><td colspan="8" class="text-center py-4 text-danger">Error al cargar la lista de préstamos.</td></tr>';
    }
}

async function abrirModalPrestamo(libroIdSeleccionado = null) {
    document.getElementById('prestamoForm').reset();
    document.getElementById('prestamoFecha').value = new Date().toISOString().split('T')[0];

    // Poblar personas
    const personaSelect = document.getElementById('prestamoPersonaSelect');
    personaSelect.innerHTML = '<option value="">Cargando personas...</option>';

    try {
        const resP = await fetch(API_URL.PERSONAS);
        const personas = await resP.json();
        personaSelect.innerHTML = '<option value="">Seleccione una persona...</option>';
        if (personas && personas.length > 0) {
            personas.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.id;
                opt.textContent = `${p.nombre} ${p.apellido} (DNI: ${p.dni})`;
                personaSelect.appendChild(opt);
            });
        }
    } catch (err) {
        personaSelect.innerHTML = '<option value="">Error al cargar personas</option>';
    }

    // Poblar libros con deshabilitación de prestados
    const libroSelect = document.getElementById('prestamoLibroSelect');
    libroSelect.innerHTML = '<option value="">Cargando libros...</option>';

    try {
        const resL = await fetch(API_URL.LIBROS);
        const libros = await resL.json();
        libroSelect.innerHTML = '<option value="">Seleccione un libro disponible...</option>';
        if (libros && libros.length > 0) {
            libros.forEach(l => {
                const opt = document.createElement('option');
                opt.value = l.id;
                if (l.prestado) {
                    opt.disabled = true;
                    opt.className = 'text-muted bg-light';
                    opt.textContent = `${l.titulo} (No disponible - Prestado)`;
                } else {
                    opt.textContent = `${l.titulo} (Disponible)`;
                    if (libroIdSeleccionado && Number(libroIdSeleccionado) === Number(l.id)) {
                        opt.selected = true;
                    }
                }
                libroSelect.appendChild(opt);
            });
        }
    } catch (err) {
        libroSelect.innerHTML = '<option value="">Error al cargar libros</option>';
    }

    prestamoModal.show();
}

function abrirModalPrestamoConLibro(libroId) {
    const prestamoTabBtn = document.getElementById('prestamos-tab');
    if (prestamoTabBtn) {
        const tab = bootstrap.Tab.getOrCreateInstance(prestamoTabBtn);
        tab.show();
    }
    abrirModalPrestamo(libroId);
}

async function guardarPrestamo(e) {
    e.preventDefault();
    const personaId = document.getElementById('prestamoPersonaSelect').value;
    const libroId = document.getElementById('prestamoLibroSelect').value;
    const fecha = document.getElementById('prestamoFecha').value;

    if (!personaId || !libroId) {
        notificar('Debe seleccionar una persona y un libro disponible.', 'warning');
        return;
    }

    // Patrón DTO estricto: AltaPrestamoDto
    const altaDto = {
        personaId: parseInt(personaId),
        libroId: parseInt(libroId),
        fechaPrestamo: fecha || null
    };

    try {
        const res = await fetch(`${API_URL.PRESTAMOS}/alta`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(altaDto)
        });

        if (!res.ok) {
            let errorMsg = 'Error al registrar el préstamo.';
            try {
                const errData = await res.json();
                if (errData && errData.error) errorMsg = errData.error;
            } catch (_) {}
            throw new Error(errorMsg);
        }

        prestamoModal.hide();
        notificar('Préstamo registrado exitosamente.', 'success');
        cargarPrestamos();
        cargarLibros();
        cargarPersonas();
    } catch (err) {
        notificar(err.message || 'Error al registrar préstamo', 'danger');
    }
}

async function devolverPrestamo(id) {
    if (!confirm(`¿Confirma la devolución del libro para el préstamo #${id}?`)) return;

    try {
        const res = await fetch(`${API_URL.PRESTAMOS}/${id}/devolver`, {
            method: 'POST'
        });

        if (!res.ok) {
            let errorMsg = 'Error al procesar la devolución.';
            try {
                const errData = await res.json();
                if (errData && errData.error) errorMsg = errData.error;
            } catch (_) {}
            throw new Error(errorMsg);
        }

        notificar('Devolución registrada correctamente. Libro disponible.', 'success');
        cargarPrestamos();
        cargarLibros();
        cargarPersonas();
    } catch (err) {
        notificar(err.message || 'Error al devolver el libro', 'danger');
    }
}

async function eliminarPrestamo(id) {
    if (!confirm(`¿Desea dar de baja el registro del préstamo #${id}?`)) return;

    try {
        const res = await fetch(`${API_URL.PRESTAMOS}/${id}`, {
            method: 'DELETE'
        });

        if (!res.ok) throw new Error('Error al dar de baja registro');

        notificar('Registro de préstamo dado de baja correctamente.', 'info');
        cargarPrestamos();
        cargarLibros();
        cargarPersonas();
    } catch (err) {
        notificar('Error al dar de baja el préstamo.', 'danger');
    }
}

// Utilitarios
function escapeQuotes(str) {
    if (!str) return '';
    return String(str).replace(/'/g, "\\'").replace(/"/g, '&quot;');
}


/* ==========================================================================
   MIGRACIÓN (Scanner, File, StringTokenizer)
   ========================================================================== */
async function ejecutarMigracionServidor() {
    const ruta = document.getElementById('migracionRutaInput').value.trim();
    const btn = document.getElementById('btnEjecutarMigracion');
    const spinner = document.getElementById('migracionSpinner');
    const panel = document.getElementById('migracionResumenPanel');

    btn.disabled = true;
    spinner.classList.remove('d-none');
    panel.classList.add('d-none');

    try {
        let url = API_URL.MIGRACION + '/ejecutar';
        if (ruta) {
            url += `?ruta=${encodeURIComponent(ruta)}`;
        }
        const res = await fetch(url, { method: 'POST' });
        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.error || 'Error al ejecutar la migración');
        }

        mostrarResultadosMigracion(data);
        notificar(`Migración finalizada: ${data.totalExitosos} registros procesados con éxito.`, 'success');
        cargarPersonas();
    } catch (err) {
        console.error(err);
        const msg = (err.name === 'TypeError' || err.message.includes('fetch') || err.message.includes('NetworkError'))
            ? 'Error de conexión: No se pudo comunicar con el servidor en http://localhost:9000. Asegúrese de que la aplicación Spring Boot esté ejecutándose.'
            : (err.message || 'Error en la migración');
        notificar(msg, 'danger');
    } finally {
        btn.disabled = false;
        spinner.classList.add('d-none');
    }
}

async function ejecutarMigracionSubida() {
    const fileInput = document.getElementById('migracionFileInput');
    if (!fileInput.files || fileInput.files.length === 0) {
        notificar('Por favor seleccione un archivo .txt para migrar', 'warning');
        return;
    }

    const file = fileInput.files[0];
    const formData = new FormData();
    formData.append('archivo', file);

    const btn = document.getElementById('btnSubirMigracion');
    const spinner = document.getElementById('migracionSpinner');
    const panel = document.getElementById('migracionResumenPanel');

    btn.disabled = true;
    spinner.classList.remove('d-none');
    panel.classList.add('d-none');

    try {
        const res = await fetch(API_URL.MIGRACION + '/subir', {
            method: 'POST',
            body: formData
        });
        const data = await res.json();

        if (!res.ok) {
            throw new Error(data.error || 'Error al subir y procesar el archivo');
        }

        mostrarResultadosMigracion(data);
        notificar(`Migración finalizada: ${data.totalExitosos} registros procesados con éxito.`, 'success');
        fileInput.value = '';
        cargarPersonas();
    } catch (err) {
        console.error(err);
        notificar(err.message || 'Error al procesar archivo subido', 'danger');
    } finally {
        btn.disabled = false;
        spinner.classList.add('d-none');
    }
}

function mostrarResultadosMigracion(data) {
    const panel = document.getElementById('migracionResumenPanel');
    panel.classList.remove('d-none');

    document.getElementById('migracionArchivoBadge').textContent = `Origen: ${data.nombreArchivo || 'migración.txt'}`;
    document.getElementById('resumenTotalLeidos').textContent = data.totalLeidos || 0;
    document.getElementById('resumenTotalExitosos').textContent = data.totalExitosos || 0;
    document.getElementById('resumenTotalFallidos').textContent = data.totalFallidos || 0;

    const tbody = document.getElementById('migracionTableBody');
    tbody.innerHTML = '';

    if (!data.registros || data.registros.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" class="text-center py-4 text-muted">No se encontraron registros en el archivo</td></tr>';
        return;
    }

    data.registros.forEach(reg => {
        let badgeEstado = '';
        if (reg.estado === 'INGRESADO') {
            badgeEstado = '<span class="badge bg-success"><i class="bi bi-check-circle me-1"></i>Ingresado</span>';
        } else if (reg.estado === 'ACTUALIZADO') {
            badgeEstado = '<span class="badge bg-info text-dark"><i class="bi bi-arrow-repeat me-1"></i>Actualizado</span>';
        } else {
            badgeEstado = '<span class="badge bg-danger"><i class="bi bi-exclamation-triangle me-1"></i>Error</span>';
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="fw-bold">${reg.numeroFila}</td>
            <td>${reg.nombre || '-'}</td>
            <td>${reg.apellido || '-'}</td>
            <td>${reg.dni ? `<span class="badge bg-light text-dark border">${reg.dni}</span>` : '-'}</td>
            <td>${reg.calle || '-'}</td>
            <td>${reg.numero != null ? reg.numero : '-'}</td>
            <td>${badgeEstado}</td>
            <td><small class="${reg.estado === 'ERROR' ? 'text-danger' : 'text-muted'}">${escapeQuotes(reg.mensaje || '')}</small></td>
        `;
        tbody.appendChild(tr);
    });
}
