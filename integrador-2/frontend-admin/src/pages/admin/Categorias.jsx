import { useState } from 'react';
import Button from '../../components/Button';
import Icon from '../../components/Icon';
import Modal from '../../components/Modal';
import { categoriesMock } from '../../mocks/categoriesMock';

export default function Categorias() {
  const [categories, setCategories] = useState(categoriesMock);
  const [selectedCatId, setSelectedCatId] = useState(categoriesMock[0]?.id || '');
  const [isCatModalOpen, setIsCatModalOpen] = useState(false);
  const [isSubCatModalOpen, setIsSubCatModalOpen] = useState(false);
  const [catNameInput, setCatNameInput] = useState('');
  const [subCatNameInput, setSubCatNameInput] = useState('');

  const selectedCategory = categories.find((c) => c.id === selectedCatId) || categories[0];

  const handleCreateCategory = (e) => {
    e.preventDefault();
    if (!catNameInput.trim()) return;
    const newCat = {
      id: `cat-${Date.now()}`,
      nombre: catNameInput.trim(),
      descripcion: `Categoría mayorista de ${catNameInput.trim()}`,
      subcategorias: []
    };
    setCategories([...categories, newCat]);
    setSelectedCatId(newCat.id);
    setCatNameInput('');
    setIsCatModalOpen(false);
    alert(`Categoría "${newCat.nombre}" creada con éxito.`);
  };

  const handleCreateSubcategory = (e) => {
    e.preventDefault();
    if (!subCatNameInput.trim() || !selectedCategory) return;
    const newSub = {
      id: `sub-${Date.now()}`,
      nombre: subCatNameInput.trim(),
      cantidadProductos: 0
    };
    setCategories((prev) =>
      prev.map((c) =>
        c.id === selectedCategory.id
          ? { ...c, subcategorias: [...c.subcategorias, newSub] }
          : c
      )
    );
    setSubCatNameInput('');
    setIsSubCatModalOpen(false);
    alert(`Subcategoría "${newSub.nombre}" agregada a ${selectedCategory.nombre}.`);
  };

  const handleDeleteCategory = (catId, nombre) => {
    if (confirm(`¿Eliminar lógicamente la categoría "${nombre}" y sus subcategorías?`)) {
      setCategories((prev) => prev.filter((c) => c.id !== catId));
      if (selectedCatId === catId) {
        setSelectedCatId(categories.find((c) => c.id !== catId)?.id || '');
      }
    }
  };

  const handleDeleteSubcategory = (subId, nombre) => {
    if (confirm(`¿Eliminar la subcategoría "${nombre}"?`)) {
      setCategories((prev) =>
        prev.map((c) =>
          c.id === selectedCategory.id
            ? { ...c, subcategorias: c.subcategorias.filter((s) => s.id !== subId) }
            : c
        )
      );
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <span className="text-[11px] font-bold tracking-[0.14em] uppercase text-[#e86bc4]">
            Estructura de Catálogo
          </span>
          <h2 className="text-xl font-bold text-[#2b2b3a] mt-0.5">
            Categorías y Subcategorías
          </h2>
          <p className="text-[12px] text-[#6b6b80] mt-0.5">
            Organización del catálogo mayorista para la tienda institucional y el portal móvil.
          </p>
        </div>

        <Button onClick={() => setIsCatModalOpen(true)}>
          <Icon name="plus" className="w-4 h-4" />
          <span>Nueva Categoría</span>
        </Button>
      </div>

      {/* Grid Maestro - Detalle */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Columna Izquierda: Categorías (5 cols) */}
        <div className="lg:col-span-5 bg-white rounded-[20px] p-5 border border-[#ececf2] shadow-sm space-y-3">
          <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
            <h3 className="text-[14px] font-bold text-[#2b2b3a]">
              Categorías Principales ({categories.length})
            </h3>
          </div>

          <div className="space-y-2">
            {categories.map((cat) => {
              const isSelected = cat.id === selectedCatId;
              const totalProds = cat.subcategorias.reduce((acc, s) => acc + s.cantidadProductos, 0);

              return (
                <div
                  key={cat.id}
                  onClick={() => setSelectedCatId(cat.id)}
                  className={`p-3.5 rounded-[14px] border cursor-pointer transition-all flex items-center justify-between ${
                    isSelected
                      ? 'border-[#e86bc4] bg-[#fff0fb] shadow-xs'
                      : 'border-[#ececf2] hover:bg-[#fafafd]'
                  }`}
                >
                  <div>
                    <h4 className="text-[13px] font-bold text-[#2b2b3a]">{cat.nombre}</h4>
                    <span className="text-[11px] text-[#6b6b80]">
                      {cat.subcategorias.length} subcategorías · {totalProds} productos
                    </span>
                  </div>

                  <div className="flex items-center gap-2">
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        handleDeleteCategory(cat.id, cat.nombre);
                      }}
                      className="text-[11px] text-[#aaa9ba] hover:text-[#ff8fa3] p-1"
                      title="Eliminar categoría"
                    >
                      ✕
                    </button>
                    <span className={`text-[12px] ${isSelected ? 'text-[#e86bc4]' : 'text-[#aaa9ba]'}`}>
                      ›
                    </span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Columna Derecha: Subcategorías de la seleccionada (7 cols) */}
        <div className="lg:col-span-7 bg-white rounded-[20px] p-6 border border-[#ececf2] shadow-sm space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-[#f0f0f4]">
            <div>
              <span className="text-[10px] text-[#aaa9ba] uppercase font-bold">Subcategorías de</span>
              <h3 className="text-lg font-bold text-[#2b2b3a]">
                {selectedCategory?.nombre}
              </h3>
            </div>

            <Button
              secondary
              onClick={() => setIsSubCatModalOpen(true)}
              className="text-[11px] px-3.5 py-1.5"
            >
              <Icon name="plus" className="w-3.5 h-3.5" />
              <span>Agregar Subcategoría</span>
            </Button>
          </div>

          <p className="text-[12px] text-[#6b6b80]">
            {selectedCategory?.descripcion}
          </p>

          <div className="divide-y divide-[#f0f0f4] pt-2">
            {selectedCategory?.subcategorias.length === 0 ? (
              <p className="text-[12px] text-[#aaa9ba] py-6 text-center">
                Esta categoría todavía no tiene subcategorías asociadas.
              </p>
            ) : (
              selectedCategory?.subcategorias.map((sub) => (
                <div key={sub.id} className="py-3 flex items-center justify-between">
                  <div className="flex items-center gap-2.5">
                    <span className="w-2 h-2 rounded-full bg-[#3fb8d9]" />
                    <div>
                      <strong className="text-[13px] text-[#2b2b3a] font-semibold">
                        {sub.nombre}
                      </strong>
                      <span className="text-[11px] text-[#aaa9ba] block">
                        {sub.cantidadProductos} productos activos en catálogo
                      </span>
                    </div>
                  </div>

                  <button
                    type="button"
                    onClick={() => handleDeleteSubcategory(sub.id, sub.nombre)}
                    className="text-[11px] text-[#ff8fa3] hover:underline cursor-pointer"
                  >
                    Eliminar
                  </button>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Modal Nueva Categoría */}
      <Modal
        isOpen={isCatModalOpen}
        onClose={() => setIsCatModalOpen(false)}
        title="Crear Nueva Categoría"
      >
        <form onSubmit={handleCreateCategory} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nombre de la Categoría *
            </label>
            <input
              type="text"
              required
              value={catNameInput}
              onChange={(e) => setCatNameInput(e.target.value)}
              placeholder="Ej: Turrones y Barquillos"
              className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>
          <div className="flex justify-end gap-2 pt-3 border-t border-[#f0f0f4]">
            <Button secondary onClick={() => setIsCatModalOpen(false)}>Cancelar</Button>
            <Button type="submit">Guardar Categoría</Button>
          </div>
        </form>
      </Modal>

      {/* Modal Nueva Subcategoría */}
      <Modal
        isOpen={isSubCatModalOpen}
        onClose={() => setIsSubCatModalOpen(false)}
        title={`Agregar Subcategoría a ${selectedCategory?.nombre}`}
      >
        <form onSubmit={handleCreateSubcategory} className="space-y-4">
          <div>
            <label className="block text-[11px] font-semibold text-[#2b2b3a] mb-1">
              Nombre de la Subcategoría *
            </label>
            <input
              type="text"
              required
              value={subCatNameInput}
              onChange={(e) => setSubCatNameInput(e.target.value)}
              placeholder="Ej: Rellenos con Fruta"
              className="w-full border border-[#ececf2] rounded-[10px] px-3 py-2 text-[12px] bg-white focus:outline-none focus:border-[#e86bc4]"
            />
          </div>
          <div className="flex justify-end gap-2 pt-3 border-t border-[#f0f0f4]">
            <Button secondary onClick={() => setIsSubCatModalOpen(false)}>Cancelar</Button>
            <Button type="submit">Agregar Subcategoría</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
