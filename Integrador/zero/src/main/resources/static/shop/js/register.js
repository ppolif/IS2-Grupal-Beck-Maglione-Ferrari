/**
 * Registro de Clientes - ZERO Sportswear
 * Gestión dinámica de Medio de Contacto, Cascada Geográfica y Previsualización de Imagen.
 */
document.addEventListener('DOMContentLoaded', function() {

  // 1. Alternancia Dinámica del Medio de Contacto (Email / Celular)
  const radioEmail = document.getElementById('contactoTipoEmail');
  const radioCelular = document.getElementById('contactoTipoCelular');
  const campoEmail = document.getElementById('campoContactoEmail');
  const campoCelular = document.getElementById('campoContactoCelular');
  const inputTelefono = document.getElementById('contactoTelefono');

  function actualizarTipoContacto() {
    if (radioCelular && radioCelular.checked) {
      if (campoEmail) campoEmail.style.display = 'none';
      if (campoCelular) campoCelular.style.display = 'block';
      if (inputTelefono) inputTelefono.required = true;
    } else {
      if (campoEmail) campoEmail.style.display = 'block';
      if (campoCelular) campoCelular.style.display = 'none';
      if (inputTelefono) {
        inputTelefono.required = false;
        inputTelefono.value = '';
      }
    }
  }

  if (radioEmail) radioEmail.addEventListener('change', actualizarTipoContacto);
  if (radioCelular) radioCelular.addEventListener('change', actualizarTipoContacto);
  actualizarTipoContacto();

  // 2. Selección Geográfica en Cascada (fetch /zonas/...)
  const paisSelect = document.getElementById('paisId');
  const provinciaSelect = document.getElementById('provinciaId');
  const departamentoSelect = document.getElementById('departamentoId');
  const localidadSelect = document.getElementById('localidadId');

  function resetSelect(selectElem, placeholder) {
    if (selectElem) {
      selectElem.innerHTML = '<option value="">' + placeholder + '</option>';
      selectElem.disabled = true;
    }
  }

  if (paisSelect) {
    paisSelect.addEventListener('change', function() {
      const paisId = this.value;
      resetSelect(provinciaSelect, '-- Seleccionar Provincia --');
      resetSelect(departamentoSelect, '-- Seleccionar Departamento --');
      resetSelect(localidadSelect, '-- Seleccionar Localidad --');

      if (!paisId) return;

      fetch('/zonas/provincias?paisId=' + encodeURIComponent(paisId))
        .then(res => res.json())
        .then(data => {
          provinciaSelect.innerHTML = '<option value="">-- Seleccionar Provincia --</option>';
          data.forEach(item => {
            const opt = document.createElement('option');
            opt.value = item.id;
            opt.textContent = item.nombre;
            provinciaSelect.appendChild(opt);
          });
          provinciaSelect.disabled = false;
        })
        .catch(err => console.error('Error al cargar provincias:', err));
    });
  }

  if (provinciaSelect) {
    provinciaSelect.addEventListener('change', function() {
      const provinciaId = this.value;
      resetSelect(departamentoSelect, '-- Seleccionar Departamento --');
      resetSelect(localidadSelect, '-- Seleccionar Localidad --');

      if (!provinciaId) return;

      fetch('/zonas/departamentos?provinciaId=' + encodeURIComponent(provinciaId))
        .then(res => res.json())
        .then(data => {
          departamentoSelect.innerHTML = '<option value="">-- Seleccionar Departamento --</option>';
          data.forEach(item => {
            const opt = document.createElement('option');
            opt.value = item.id;
            opt.textContent = item.nombre;
            departamentoSelect.appendChild(opt);
          });
          departamentoSelect.disabled = false;
        })
        .catch(err => console.error('Error al cargar departamentos:', err));
    });
  }

  if (departamentoSelect) {
    departamentoSelect.addEventListener('change', function() {
      const departamentoId = this.value;
      resetSelect(localidadSelect, '-- Seleccionar Localidad --');

      if (!departamentoId) return;

      fetch('/zonas/localidades?departamentoId=' + encodeURIComponent(departamentoId))
        .then(res => res.json())
        .then(data => {
          localidadSelect.innerHTML = '<option value="">-- Seleccionar Localidad --</option>';
          data.forEach(item => {
            const opt = document.createElement('option');
            opt.value = item.id;
            opt.textContent = item.nombre + (item.codigoPostal ? ' (CP: ' + item.codigoPostal + ')' : '');
            localidadSelect.appendChild(opt);
          });
          localidadSelect.disabled = false;
        })
        .catch(err => console.error('Error al cargar localidades:', err));
    });
  }

  // 3. Previsualización Instantánea de Foto de Perfil
  const fotoInput = document.getElementById('fotoPerfil');
  const previewContainer = document.getElementById('previewContainer');
  const imagePreview = document.getElementById('imagePreview');

  if (fotoInput) {
    fotoInput.addEventListener('change', function(e) {
      const file = e.target.files && e.target.files[0];
      if (file) {
        const objectUrl = URL.createObjectURL(file);
        if (imagePreview) imagePreview.src = objectUrl;
        if (previewContainer) previewContainer.style.display = 'block';
      }
    });
  }
});
