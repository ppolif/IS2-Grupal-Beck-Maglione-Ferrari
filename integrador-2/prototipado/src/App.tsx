import { useState } from 'react';

type Section = 'system' | 'site' | 'sales' | 'delivery' | 'manager' | 'warehouse';

const candy =
  'https://images.unsplash.com/photo-1633158832466-be592c721217?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&q=85&w=1200';
const macarons =
  'https://images.unsplash.com/photo-1666274694243-9997eb427237?crop=entropy&cs=tinysrgb&fit=crop&fm=jpg&q=85&w=1200';

const sections: { id: Section; label: string; short: string }[] = [
  { id: 'system', label: '0 Sistema de diseño', short: 'Sistema' },
  { id: 'site', label: '1 Institucional', short: 'Tienda' },
  { id: 'sales', label: '2 Preventores', short: 'Preventores' },
  { id: 'delivery', label: '3 Distribuidores', short: 'Distribuidores' },
  { id: 'manager', label: '4 Jefe Admin', short: 'Jefe Admin' },
  { id: 'warehouse', label: '5 Admin', short: 'Almacén' },
];

const products = [
  ['Alfajor triple chocolate', 'Caja x 24 u.', '$18.900', macarons],
  ['Gomitas frutales', 'Bolsa x 1 kg', '$6.450', candy],
  ['Chupetines surtidos', 'Display x 50 u.', '$8.200', candy],
  ['Chocolate con maní', 'Caja x 20 u.', '$15.600', macarons],
];

function Logo({ light = false }: { light?: boolean }) {
  return (
    <div className={`logo ${light ? 'light' : ''}`}>
      <span className="candy-mark"><i /><b /></span>
      <span>Dulce Mayor</span>
    </div>
  );
}

function Icon({ name }: { name: string }) {
  const paths: Record<string, React.ReactNode> = {
    home: <><path d="M3 11 12 3l9 8" /><path d="M5 10v10h14V10M9 20v-6h6v6" /></>,
    bag: <><path d="M5 8h14l-1 12H6L5 8Z" /><path d="M9 8a3 3 0 0 1 6 0" /></>,
    route: <><circle cx="6" cy="18" r="2" /><circle cx="18" cy="6" r="2" /><path d="M8 18h3a3 3 0 0 0 3-3V9a3 3 0 0 1 3-3" /></>,
    user: <><circle cx="12" cy="8" r="4" /><path d="M4 21a8 8 0 0 1 16 0" /></>,
    chart: <><path d="M4 20V9M10 20V4M16 20v-7M22 20H2" /></>,
    box: <><path d="m4 7 8-4 8 4-8 4-8-4Z" /><path d="M4 7v10l8 4 8-4V7M12 11v10" /></>,
  };
  return <svg className="icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8">{paths[name] || paths.box}</svg>;
}

function Button({ children, secondary = false }: { children: React.ReactNode; secondary?: boolean }) {
  return <button className={secondary ? 'btn secondary' : 'btn'}>{children}</button>;
}

function ProductCard({ item = products[0] }: { item?: string[] }) {
  return (
    <article className="product-card">
      <img src={item[3]} alt="" />
      <span className="eyebrow">Precio mayorista</span>
      <h3>{item[0]}</h3>
      <p>{item[1]}</p>
      <div className="product-foot"><strong>{item[2]}</strong><button className="round-add">+</button></div>
    </article>
  );
}

function SystemScreen() {
  return (
    <div className="design-page">
      <header className="design-hero">
        <Logo />
        <div><span className="tag">UI KIT · v1.0</span><h1>Un sistema tan simple<br />como abrir un caramelo.</h1><p>Lenguaje visual para los cinco productos digitales de Dulce Mayor.</p></div>
        <div className="hero-sweets"><span /><span /><span /></div>
      </header>
      <section className="ds-section">
        <div className="section-title"><span>01</span><div><h2>Colores</h2><p>Pasteles luminosos con contraste claro y accesible.</p></div></div>
        <div className="swatches">
          {[['#FFCEF3','Rosa suave'],['#E86BC4','Rosa intenso'],['#A1EAFB','Celeste'],['#3FB8D9','Celeste intenso'],['#2B2B3A','Tinta'],['#7ED6A5','Éxito']].map(([c,n])=><div className="swatch" key={c}><i style={{background:c}}/><b>{n}</b><small>{c}</small></div>)}
        </div>
      </section>
      <section className="ds-grid">
        <div className="spec-card typography"><span className="tag">TIPOGRAFÍA</span><div className="mega">Aa</div><h2>Poppins</h2><p>Semibold para decisiones. Regular para lectura.</p><div className="type-row"><b>Las mejores golosinas</b><span>32 / Semibold</span></div><div className="type-row"><p>Atención a comercios</p><span>16 / Regular</span></div></div>
        <div className="spec-card"><span className="tag">BOTONES & ESTADOS</span><div className="button-demo"><Button>Acción principal</Button><Button secondary>Acción secundaria</Button></div><div className="chips"><span className="chip warning">Pendiente</span><span className="chip blue">En camino</span><span className="chip success">Entregado</span><span className="chip pink">Visitado</span></div><div className="input-demo"><label>Nombre del comercio</label><div>Kiosco El Sol</div></div></div>
      </section>
    </div>
  );
}

function SiteScreen() {
  return (
    <div className="website">
      <nav className="web-nav"><Logo /><div className="web-links"><b>Inicio</b><span>Tienda</span><span>Sucursales</span><span>Contacto</span></div><div><Button secondary>Ingresar</Button><Button>Registrarme</Button></div></nav>
      <main>
        <section className="web-hero">
          <div><span className="tag white">MAYORISTA DE MENDOZA</span><h1>Las mejores golosinas<br />al por mayor</h1><p>Todo lo que tu comercio necesita, con precios que dan gusto.</p><Button>Ver la tienda <span>→</span></Button><div className="trust"><b>+450</b> comercios eligen Dulce Mayor</div></div>
          <div className="photo-stack"><img src={candy} alt="Golosinas coloridas" /><div className="dollar"><small>DÓLAR HOY</small><b>$ 1.185</b><span>Actualizado 10:32</span></div></div>
        </section>
        <section className="shop-preview"><div className="heading-row"><div><span className="eyebrow">PARA TU NEGOCIO</span><h2>Una muestra de nuestra tienda</h2></div><Button secondary>Ver catálogo completo</Button></div><div className="product-grid">{products.map((p)=><ProductCard key={p[0]} item={p}/>)}</div></section>
        <section className="benefits">{[['box','Precios mayoristas','Comprá por bulto y hacé crecer tu margen.'],['route','Entrega a domicilio','Llegamos a tu comercio en todo Mendoza.'],['user','Atención cercana','Te asesora una persona que conoce tu zona.']].map(x=><div><span className="benefit-icon"><Icon name={x[0]}/></span><h3>{x[1]}</h3><p>{x[2]}</p></div>)}</section>
      </main>
      <div className="chat"><span>¿Te armo un presupuesto?</span><b>DM</b></div>
    </div>
  );
}

function MiniMap({ delivery = false }: { delivery?: boolean }) {
  return <div className="map"><div className="road r1"/><div className="road r2"/><div className="road r3"/><div className="road r4"/><svg viewBox="0 0 390 520"><path d={delivery ? "M80 450 C100 350 280 380 260 250 S120 180 300 80" : "M76 430 C180 390 120 280 230 240 S300 140 275 70"} fill="none" stroke={delivery ? "#3FB8D9" : "#E86BC4"} strokeWidth="7" strokeLinecap="round" strokeDasharray="1 1"/><circle cx="76" cy="430" r="14" fill="#fff"/><circle cx="76" cy="430" r="8" fill="#3FB8D9"/></svg><span className="pin">●</span><div className="map-label">Av. San Martín</div><div className="map-label two">Godoy Cruz</div></div>;
}

const mobileMenus = {
  sales: [['Registro','box'],['Recorrido','route'],['Tienda','home'],['Carrito','bag'],['Perfil','user']],
  delivery: [['Pedidos','box'],['Recorrido','route'],['Perfil','user']],
};

function BottomNav({ type, active }: { type: 'sales'|'delivery'; active: string }) {
  return <div className="bottom-nav">{mobileMenus[type].map(([l,i])=><div className={l===active?'active':''} key={l}><Icon name={i}/><span>{l}</span></div>)}</div>
}

function MobileShell({ type }: { type: 'sales' | 'delivery' }) {
  const [view,setView] = useState(type==='sales'?'Recorrido':'Pedidos');
  const menu=mobileMenus[type].map(x=>x[0]);
  return <div className="mobile-stage"><div className="phone">
    <div className="phone-top"><Logo /><span className="avatar">LM</span></div>
    {view==='Recorrido' ? <><MiniMap delivery={type==='delivery'}/><div className="sheet"><i/><div className="sheet-head"><span className="chip pink">{type==='delivery'?'Entrega 4 de 10':'3 de 12 visitas'}</span><small>{type==='delivery'?'12 min · 3,2 km':'8 min · 2,4 km'}</small></div><h2>{type==='delivery'?'Almacén Don Pepe':'Kiosco El Sol'}</h2><p>Av. San Martín 1842, Godoy Cruz</p>{type==='delivery'&&<div className="ticket"><span>Pedido #DM-2084</span><b>$45.200 · Efectivo</b><small>3 cajas alfajores · 2 bolsas gomitas</small></div>}<div className="sheet-actions"><Button>{type==='delivery'?'Marcar como entregado':'Marcar como visitado'}</Button><Button secondary>Dejar pendiente</Button></div></div></> :
    <MobileContent type={type} view={view}/>}
    <div className="bottom-nav">{mobileMenus[type].map(([l,i])=><button onClick={()=>setView(l)} className={l===view?'active':''} key={l}><Icon name={i}/><span>{l}</span></button>)}</div>
  </div><div className="mobile-notes"><span className="tag">FRONTEND {type==='sales'?'02':'03'}</span><h1>{type==='sales'?'App de preventores':'App de distribuidores'}</h1><p>Diseñada para decisiones rápidas en movimiento, con objetivos táctiles amplios y jerarquía directa.</p><div className="screen-index">{menu.map((m,i)=><button onClick={()=>setView(m)} className={m===view?'active':''}><b>{type==='sales'?'2':'3'}{String.fromCharCode(65+i)}</b><span>{m}</span></button>)}</div></div></div>
}

function MobileContent({type,view}:{type:'sales'|'delivery';view:string}) {
  if(view==='Perfil') return <div className="mobile-content"><span className="tag">MI CUENTA</span><div className="profile-card"><div className="big-avatar">LM</div><h2>Lucía Martínez</h2><p>{type==='sales'?'Zona Godoy Cruz':'Unidad 08 · Gran Mendoza'}</p><div className="stat-pair"><div><b>8</b><span>{type==='sales'?'Visitas':'Entregas'}</span></div><div><b>4</b><span>Pendientes</span></div></div></div><div className="option">Cambiar contraseña <span>→</span></div><div className="option danger">Cerrar sesión <span>→</span></div></div>;
  if(view==='Pedidos') return <div className="mobile-content orders"><span className="tag">HOY · 14 JUN</span><h1>10 entregas asignadas</h1>{['Kiosco El Sol','Almacén Don Pepe','Drugstore Mitre'].map((n,i)=><div className="order-card"><div><span>DM-20{84+i}</span><h3>{n}</h3><p>Godoy Cruz · 2,4 km</p></div><strong className={i?'paid':'cash'}>{i?'Transferencia · pagado':'Cobrar $45.200'}</strong></div>)}</div>;
  if(view==='Registro') return <div className="mobile-content"><span className="tag">NUEVO MINORISTA</span><h1>Registrar comercio</h1>{['Nombre del comercio','Responsable','CUIT','Teléfono','Email','Dirección'].map((x,i)=><div className="field"><label>{x}</label><span>{i===0?'Kiosco El Sol':''}</span></div>)}<Button>Registrar comercio</Button></div>;
  if(view==='Carrito') return <div className="mobile-content"><span className="tag">PEDIDO EN CURSO</span><h1>Tu carrito</h1>{products.slice(0,2).map(p=><div className="cart-line"><img src={p[3]}/><div><b>{p[0]}</b><small>{p[1]}</small><span>− &nbsp; 2 &nbsp; +</span></div><strong>{p[2]}</strong></div>)}<div className="total"><span>Subtotal <b>$50.100</b></span><span>Descuento <b>−$4.900</b></span><strong>Total <b>$45.200</b></strong></div><Button>Confirmar pedido</Button></div>;
  return <div className="reuse"><Icon name="home"/><h2>Pantalla reutilizada del Frontend 1</h2><p>Catálogo adaptado a mobile.</p></div>;
}

function Bars() { return <div className="bar-chart">{[55,78,46,88,64,92,70].map((h,i)=><div><i style={{height:`${h}%`}}/><span>{['L','M','X','J','V','S','D'][i]}</span></div>)}</div> }
function LineChart(){return <div className="line-chart"><svg viewBox="0 0 600 180" preserveAspectRatio="none"><path d="M0 150 C70 135 100 30 170 80 S260 160 330 85 S430 20 470 75 S540 130 600 35" fill="none" stroke="#E86BC4" strokeWidth="5"/><path d="M0 165 C90 150 110 110 190 130 S300 120 350 140 S460 70 600 95" fill="none" stroke="#3FB8D9" strokeWidth="4"/></svg></div>}

function Dashboard({ warehouse=false }: { warehouse?: boolean }) {
  const [view,setView]=useState(warehouse?'Pedidos':'Dashboard');
  const menus=warehouse?['Pedidos','Stock','Productos','Rutas del día','Pagos','Perfil']:['Dashboard','Estadísticas','Reportes','Rendimiento','Usuarios','Promociones','Perfil'];
  return <div className="dash-wrap"><aside><Logo/><div className="side-label">GESTIÓN</div>{menus.map((m,i)=><button onClick={()=>setView(m)} className={view===m?'active':''}><Icon name={i%3===0?'home':i%2?'chart':'box'}/><span>{m}</span></button>)}<div className="side-help"><b>Centro de ayuda</b><p>¿Necesitás asistencia?</p></div></aside><main className="dash-main"><header><div><span className="crumb">Dulce Mayor / {view}</span><h1>{view}</h1></div><div className="header-tools"><div className="search">⌕ &nbsp; Buscar...</div><span className="bell">○</span><span className="avatar">MS</span></div></header><DashboardContent warehouse={warehouse} view={view}/></main></div>
}

function DashboardContent({warehouse,view}:{warehouse:boolean;view:string}) {
  if(view==='Rutas del día') return <><div className="tabs"><b>Preventores</b><span>Distribuidores</span></div><div className="routes-layout"><div className="route-list">{['Lucía Martínez','Martín Flores','Sofía Vidal'].map((n,i)=><div><span className="avatar">{n.split(' ').map(x=>x[0]).join('')}</span><section><b>{n}</b><small>{i+4} de {i+9} paradas</small><i><em style={{width:`${45+i*18}%`}}/></i></section></div>)}</div><MiniMap delivery/></div></>;
  if(view==='Productos') return <div className="form-card"><div><span className="tag">CATÁLOGO</span><h2>Alta de producto</h2><p>Completá la información comercial y de stock.</p></div><div className="form-grid">{['Nombre','Categoría','Presentación','Precio mayorista','Stock inicial','Descripción'].map(x=><div className="field"><label>{x}</label><span>Ingresar {x.toLowerCase()}</span></div>)}</div><div className="upload">＋<b>Cargar imagen</b><span>PNG o JPG hasta 5 MB</span></div><Button>Guardar producto</Button></div>;
  if(view==='Stock') return <><Kpis warehouse/><div className="module"><div className="module-title"><div><h2>Inventario</h2><p>Actualizado hace 5 minutos</p></div><Button>Nuevo producto</Button></div><DataTable stock/></div></>;
  if(view==='Usuarios') return <><div className="module"><div className="module-title"><div><h2>Usuarios del sistema</h2><p>Administrá accesos y roles</p></div><Button>Nuevo usuario</Button></div><DataTable users/></div><div className="modal-demo"><span className="tag">NUEVO USUARIO</span><h3>Crear acceso</h3><div className="field"><label>Nombre completo</label><span>Ingresar nombre</span></div><div className="field"><label>Email</label><span>nombre@dulcemayor.com</span></div><p>Clave inicial: <b>dulcemayor</b>. Deberá cambiarla en el primer ingreso.</p><Button>Crear usuario</Button></div></>;
  if(view==='Reportes'||view==='Rendimiento'||view==='Pagos') return <><div className="filterbar"><span>Período: Junio 2025</span><span>Departamento: Todos</span><span>{warehouse?'Medio de pago':'Preventor'}: Todos</span><Button secondary>{warehouse?'Registrar pago':'Descargar PDF'}</Button></div><div className="module"><div className="module-title"><h2>{view==='Rendimiento'?'Ranking de preventores':view==='Pagos'?'Estado de pagos':'Ventas históricas'}</h2></div><DataTable/></div></>;
  return <><Kpis warehouse={warehouse}/><div className="dash-grid"><div className="module wide"><div className="module-title"><div><h2>{warehouse?'Pedidos por estado':'Ventas del mes'}</h2><p>{warehouse?'Flujo operativo':'Comparativo diario'}</p></div><span className="chip blue">Junio 2025</span></div><LineChart/></div><div className="module"><div className="module-title"><h2>{warehouse?'Actividad semanal':'Pedidos por preventor'}</h2></div><Bars/></div></div><div className="dash-grid lower"><div className="module wide"><div className="module-title"><h2>{warehouse?'Pedidos recientes':'Últimos pedidos'}</h2><span className="eyebrow">VER TODOS</span></div><DataTable/></div><div className="module donut-module"><div className="module-title"><h2>Stock por categoría</h2></div><div className="donut"><div><b>1.284</b><span>unidades</span></div></div><div className="legend"><span>Chocolates 42%</span><span>Golosinas 34%</span><span>Otros 24%</span></div></div></div></>;
}

function Kpis({warehouse}:{warehouse:boolean}){const items=warehouse?[['Pedidos hoy','48','+12%'],['Stock bajo','8','Revisar'],['Sin stock','3','Urgente'],['En distribución','21','En curso']]:[['Ventas del mes','$12,8 M','+18,4%'],['Pedidos','1.284','+8,2%'],['Clientes activos','456','+24'],['Ticket promedio','$38.450','+5,1%']];return <div className="kpi-grid">{items.map((x,i)=><div className="kpi"><span className={`kpi-icon k${i}`}><Icon name={i%2?'bag':'chart'}/></span><div><small>{x[0]}</small><b>{x[1]}</b><em>{x[2]}</em></div></div>)}</div>}
function DataTable({stock=false,users=false}:{stock?:boolean;users?:boolean}){const rows=stock?[['Alfajor triple chocolate','Alfajores','Caja x 24','128 u.'],['Gomitas frutales','Golosinas','Bolsa x 1 kg','12 u.'],['Chupetines surtidos','Chupetines','Display x 50','0 u.']]:users?[['Lucía Martínez','Preventor','Activo','Editar'],['Marcos Silva','Administrativo','Activo','Editar'],['Sofía Vidal','Preventor','Pausado','Editar']]:[['#DM-2084','Kiosco El Sol','Lucía Martínez','$45.200'],['#DM-2083','Almacén Don Pepe','Martín Flores','$81.450'],['#DM-2082','Drugstore Mitre','Sofía Vidal','$32.800'],['#DM-2081','Kiosco Central','Lucía Martínez','$64.120']];return <div className="table"><div className="tr th">{(stock?['Producto','Categoría','Presentación','Stock']:users?['Nombre','Rol','Estado','Acción']:['Pedido','Comercio','Preventor','Monto']).map(x=><span>{x}</span>)}</div>{rows.map((r,i)=><div className="tr">{r.map((x,j)=><span className={j===3?'strong':''}>{x}{j===0&&i<2&&!users&&!stock?<small className="dot-status">En camino</small>:null}</span>)}</div>)}</div>}

export default function App() {
  const [section,setSection]=useState<Section>('system');
  return <div className="app"><div className="prototype-bar"><Logo light/><div className="proto-tabs">{sections.map(s=><button className={section===s.id?'active':''} onClick={()=>setSection(s.id)}><span>{s.label.split(' ')[0]}</span>{s.short}</button>)}</div><div className="proto-meta"><span>Prototipo visual</span><b>1440 / 390</b></div></div>
    <div className="canvas">
      {section==='system'&&<SystemScreen/>}
      {section==='site'&&<SiteScreen/>}
      {section==='sales'&&<MobileShell type="sales"/>}
      {section==='delivery'&&<MobileShell type="delivery"/>}
      {section==='manager'&&<Dashboard/>}
      {section==='warehouse'&&<Dashboard warehouse/>}
    </div>
  </div>
}
