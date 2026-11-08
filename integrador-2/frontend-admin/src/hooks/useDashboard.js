import {
  monthlySalesChart,
  weeklyActivityChart,
  categoryDonutChart,
} from '../mocks/reports';
import { ordersMock } from '../mocks/orders';

/**
 * Custom hook providing data and computed values for the Executive Dashboard.
 */
export function useDashboard() {
  const recentOrders = ordersMock.slice(0, 4);

  return {
    recentOrders,
    monthlySalesChart,
    weeklyActivityChart,
    categoryDonutChart,
  };
}
