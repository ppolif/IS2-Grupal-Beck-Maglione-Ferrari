import Icon from './Icon';

export default function Kpis({ warehouse = false }) {
  const items = warehouse
    ? [
        { label: 'Pedidos hoy', val: '48', badge: '+12%', icon: 'chart', isBlue: false },
        { label: 'Stock bajo', val: '8', badge: 'Revisar', icon: 'bag', isBlue: true },
        { label: 'Sin stock', val: '3', badge: 'Urgente', icon: 'box', isBlue: false },
        { label: 'En distribución', val: '21', badge: 'En curso', icon: 'route', isBlue: true }
      ]
    : [
        { label: 'Ventas del mes', val: '$12,8 M', badge: '+18,4%', icon: 'chart', isBlue: false },
        { label: 'Pedidos', val: '1.284', badge: '+8,2%', icon: 'bag', isBlue: true },
        { label: 'Clientes activos', val: '456', badge: '+24', icon: 'user', isBlue: false },
        { label: 'Ticket promedio', val: '$38.450', badge: '+5,1%', icon: 'box', isBlue: true }
      ];

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3.5">
      {items.map((x, i) => (
        <div
          key={x.label}
          className="bg-white rounded-[18px] p-4 sm:p-4.5 border border-[#ececf2] shadow-sm flex items-center gap-3.5"
        >
          <span
            className={`w-[45px] h-[45px] rounded-[14px] flex items-center justify-center shrink-0 ${
              x.isBlue
                ? 'bg-[#e1f8fd] text-[#3fb8d9]'
                : 'bg-[#ffe9f9] text-[#e86bc4]'
            }`}
          >
            <Icon name={x.icon} className="w-5 h-5" />
          </span>
          <div className="flex flex-col">
            <small className="text-[10px] text-[#9b99a8] font-medium leading-none">
              {x.label}
            </small>
            <b className="text-[19px] text-[#2b2b3a] font-bold mt-1 leading-tight">
              {x.val}
            </b>
            <em className="not-italic text-[9px] font-semibold text-[#40a56a] mt-0.5">
              {x.badge}
            </em>
          </div>
        </div>
      ))}
    </div>
  );
}

