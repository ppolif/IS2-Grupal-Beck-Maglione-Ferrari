/**
 * Gestión interactiva del carrito de compras (Zero Shop)
 * - Actualización reactiva de cantidades con flechas o teclado
 * - Advertencia y restricción por límite de stock disponible
 * - Recálculo automático en tiempo real de subtotales y total del carrito
 * - Sincronización asíncrona con el backend
 */
document.addEventListener('DOMContentLoaded', function () {
    const qtyInputs = document.querySelectorAll('.cart-qty-input');
    const alertArea = document.getElementById('cartAlertArea');
    const cartTotalDisplay = document.getElementById('cartTotalDisplay');
    const modalCartTotalDisplay = document.getElementById('modalCartTotalDisplay');
    const pendingRequests = {};

    function showAlert(message, type = 'warning') {
        if (!alertArea) return;
        alertArea.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show mb-4 shadow-sm" role="alert" style="transition: all 0.3s ease;">
                <i class="fa ${type === 'danger' ? 'fa-exclamation-circle' : 'fa-exclamation-triangle'} mr-2"></i>
                <span>${message}</span>
                <button type="button" class="close" data-dismiss="alert" aria-label="Cerrar">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>
        `;
        alertArea.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function clearAlert() {
        if (alertArea) {
            alertArea.innerHTML = '';
        }
    }

    function formatMoney(amount) {
        return Number(amount).toFixed(2);
    }

    function recalculateTotalsLocally() {
        let total = 0;
        document.querySelectorAll('.cart-qty-input').forEach(function (input) {
            const price = parseFloat(input.dataset.price) || 0;
            const qty = parseInt(input.value, 10) || 0;
            const subtotal = Math.round((price * qty) * 100) / 100;
            total += subtotal;

            const itemId = input.dataset.itemId;
            const subtotalEl = document.getElementById('subtotal-' + itemId);
            if (subtotalEl) {
                subtotalEl.textContent = formatMoney(subtotal);
            }
        });

        total = Math.round(total * 100) / 100;
        if (cartTotalDisplay) cartTotalDisplay.textContent = formatMoney(total);
        if (modalCartTotalDisplay) modalCartTotalDisplay.textContent = formatMoney(total);
    }

    function updateQuantity(input) {
        const itemId = input.dataset.itemId;
        const maxStock = parseInt(input.dataset.stock, 10);
        const productName = input.dataset.productName || 'Producto';
        let rawVal = input.value;
        let val = parseInt(rawVal, 10);

        if (isNaN(val) || val < 1) {
            val = 1;
            input.value = val;
        }

        // Advertencia y ajuste automático si supera el stock disponible
        if (!isNaN(maxStock) && maxStock > 0 && val > maxStock) {
            val = maxStock;
            input.value = maxStock;
            showAlert(`No se dispone de suficiente stock para <strong>${productName}</strong>. La cantidad solicitada supera el límite y se ajustó al máximo disponible (<strong>${maxStock}</strong> unidades).`, 'warning');
        } else if (!isNaN(maxStock) && maxStock === 0) {
            val = 1;
            input.value = 1;
            showAlert(`El producto <strong>${productName}</strong> actualmente no cuenta con stock disponible.`, 'danger');
        }

        // Recálculo visual inmediato de subtotales y total general
        recalculateTotalsLocally();

        // Enviar al backend vía formulario Spring MVC si se activa por cambio
        if (input.form) {
            input.form.submit();
        }
    }

    qtyInputs.forEach(function (input) {
        input.addEventListener('change', function () {
            updateQuantity(this);
        });
    });
});
