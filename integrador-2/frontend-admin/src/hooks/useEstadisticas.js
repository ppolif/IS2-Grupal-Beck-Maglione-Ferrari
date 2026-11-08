import {
  weeklyActivityChart,
  categoryDonutChart
} from '../mocks/reports';

export function useEstadisticas() {
  const annualGrowthData = [
    { mes: 'Ene', facturado: 8.5 },
    { mes: 'Feb', facturado: 9.2 },
    { mes: 'Mar', facturado: 10.4 },
    { mes: 'Abr', facturado: 11.1 },
    { mes: 'May', facturado: 11.9 },
    { mes: 'Jun', facturado: 12.8 }
  ];

  return {
    annualGrowthData,
    weeklyActivityChart,
    categoryDonutChart
  };
}
