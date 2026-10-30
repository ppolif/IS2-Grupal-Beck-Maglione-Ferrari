import { useEffect, useRef } from 'react';
import Button from './Button';

/**
 * Modal nativo de confirmación accesible con fondo difuminado (::backdrop)
 * y foco seguro inicial en la acción 'Rechazar'.
 */
export default function ConfirmDeleteModal({
  isOpen,
  onClose,
  onConfirm,
  loading = false,
  title = "Confirmar eliminación",
  message = "Esta seguro que desea eliminar esta cuenta? Esta accion no se puede deshacer en un futuro.",
}) {
  const dialogRef = useRef(null);

  useEffect(() => {
    const dialog = dialogRef.current;
    if (!dialog) return;

    if (isOpen) {
      if (!dialog.open) {
        dialog.showModal();
      }
    } else {
      if (dialog.open) {
        dialog.close();
      }
    }
  }, [isOpen]);

  const handleCancel = (e) => {
    e.preventDefault();
    if (!loading) {
      onClose();
    }
  };

  return (
    <dialog
      ref={dialogRef}
      onCancel={handleCancel}
      aria-labelledby="confirm-delete-title"
      aria-describedby="confirm-delete-desc"
      className="m-auto rounded-[24px] p-6 sm:p-8 max-w-md w-[calc(100%-2rem)] border border-[#ececf2] bg-white shadow-[0_22px_80px_rgba(32,30,50,0.28)] backdrop:bg-[#201e32]/45 backdrop:backdrop-blur-md focus:outline-none"
    >
      <div className="space-y-5 text-center">
        <div className="w-14 h-14 mx-auto rounded-full bg-[#ffe8ed] text-[#ff8fa3] flex items-center justify-center text-2xl font-bold shadow-xs">
          !
        </div>

        <div>
          <h2 id="confirm-delete-title" className="text-xl font-bold text-[#2b2b3a]">
            {title}
          </h2>
          <p id="confirm-delete-desc" className="text-[13px] text-[#6b6b80] mt-2 leading-relaxed">
            {message}
          </p>
        </div>

        <div className="flex flex-col-reverse sm:flex-row gap-3 pt-2 justify-center">
          <Button
            secondary
            onClick={onClose}
            disabled={loading}
            className="w-full sm:w-auto px-6 py-2.5 text-[13px]"
          >
            Rechazar
          </Button>
          <Button
            variant="danger"
            onClick={onConfirm}
            disabled={loading}
            className="w-full sm:w-auto px-6 py-2.5 text-[13px]"
          >
            {loading ? 'Eliminando...' : 'Aceptar'}
          </Button>
        </div>
      </div>
    </dialog>
  );
}

