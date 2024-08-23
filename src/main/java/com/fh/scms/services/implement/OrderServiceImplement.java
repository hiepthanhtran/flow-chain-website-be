package com.fh.scms.services.implement;

import com.fh.scms.dto.order.OrderDetailsReponse;
import com.fh.scms.dto.order.OrderRequest;
import com.fh.scms.dto.order.OrderResponse;
import com.fh.scms.enums.OrderStatus;
import com.fh.scms.pojo.*;
import com.fh.scms.repository.InvoiceRepository;
import com.fh.scms.repository.OrderDetailsRepository;
import com.fh.scms.repository.OrderRepository;
import com.fh.scms.repository.TaxRepository;
import com.fh.scms.services.InventoryService;
import com.fh.scms.services.OrderService;
import com.fh.scms.services.ProductService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImplement implements OrderService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailsRepository orderDetailsRepository;
    @Autowired
    private ProductService productService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private TaxRepository taxRepository;

    @Override
    public OrderResponse getOrderResponse(@NotNull Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .type(order.getType())
                .status(order.getStatus())
                .orderDate(order.getCreatedAt())
                .expectedDelivery(order.getExpectedDelivery())
                .orderDetailsSet(order.getOrderDetailsSet()
                        .stream()
                        .map(this::getOrderDetailsReponse)
                        .collect(Collectors.toSet()))
                .build();
    }

    @Override
    public OrderDetailsReponse getOrderDetailsReponse(@NotNull OrderDetails orderDetails) {
        return OrderDetailsReponse.builder()
                .id(orderDetails.getId())
                .product(this.productService.getProductResponse(orderDetails.getProduct()))
                .quantity(orderDetails.getQuantity())
                .unitPrice(orderDetails.getUnitPrice())
                .build();
    }

    @Override
    public List<OrderResponse> getAllOrderResponse(Map<String, String> params) {
        return this.orderRepository.findAllWithFilter(params).stream()
                .map(this::getOrderResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void checkout(User user, OrderRequest orderRequest) {
        if (orderRequest != null) {
            Order order = Order.builder()
                    .user(user)
                    .type(orderRequest.getType())
                    .build();
            this.orderRepository.save(order);

            final BigDecimal[] totalAmount = {BigDecimal.ZERO};
            orderRequest.getOrderDetails().forEach(orderDetailsRequest -> {
                Product product = this.productService.findById(orderDetailsRequest.getProductId());

                Inventory inventory = product.getInventory();
                if (inventory.getQuantity() < orderDetailsRequest.getQuantity()) {
                    throw new IllegalArgumentException("Số lượng sản phẩm không đủ trong kho: " + inventory.getName());
                }

                OrderDetails orderDetails = OrderDetails.builder()
                        .order(order)
                        .product(this.productService.findById(orderDetailsRequest.getProductId()))
                        .quantity(orderDetailsRequest.getQuantity())
                        .unitPrice(orderDetailsRequest.getUnitPrice())
                        .build();
                this.orderDetailsRepository.save(orderDetails);

                totalAmount[0] = totalAmount[0].add(orderDetailsRequest.getUnitPrice()
                        .multiply(BigDecimal.valueOf(orderDetailsRequest.getQuantity())));
            });

            Invoice invoice = Invoice.builder()
                    .user(user)
                    .order(order)
                    .tax(this.taxRepository.findByRegion("VN"))
                    .totalAmount(totalAmount[0])
                    .build();
            this.invoiceRepository.save(invoice);
        }
    }

    @Override
    public void cancelOrder(User user, Long orderId) {
        Order order = this.orderRepository.findById(orderId);

        if (order == null || !Objects.equals(order.getUser().getId(), user.getId())) {
            throw new EntityNotFoundException("Không tìm thấy đơn hàng");
        }

        switch (order.getStatus()) {
            case CANCELLED:
                throw new IllegalStateException("Đơn hàng đã bị hủy");
            case CONFIRMED:
            case SHIPPED:
            case DELIVERED:
                throw new IllegalStateException("Đơn hàng không thể hủy vì đã được xác nhận");
            default:
                break;
        }

        order.setCancel(true);
        order.setStatus(OrderStatus.CANCELLED);
        this.orderRepository.update(order);

        Invoice invoice = this.invoiceRepository.findByOrderId(orderId);
        this.invoiceRepository.delete(invoice.getId());
    }

    @Override
    public void updateOrderStatus(Long orderId, String status) {
        Order order = this.orderRepository.findById(orderId);

        if (order == null) {
            throw new EntityNotFoundException("Không tìm thấy đơn hàng");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng đã bị hủy");
        }

        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ");
        }

        Set<OrderDetails> orderDetails = order.getOrderDetailsSet();
        List<Product> products = orderDetails.stream()
                .map(OrderDetails::getProduct)
                .collect(Collectors.toList());
        Float totalQuantity = orderDetails.stream()
                .map(OrderDetails::getQuantity)
                .reduce(0F, Float::sum);

        switch (orderStatus) {
            case DELIVERED:
                products.forEach(product -> {
                    product.getInventory().setQuantity(product.getInventory().getQuantity() - totalQuantity);
                    this.inventoryService.update(product.getInventory());
                });
                break;
            case CANCELLED:
                products.forEach(product -> {
                    product.getInventory().setQuantity(product.getInventory().getQuantity() + totalQuantity);
                    this.inventoryService.update(product.getInventory());
                });

                Invoice invoice = this.invoiceRepository.findByOrderId(orderId);
                this.invoiceRepository.delete(invoice.getId());
                break;
            case RETURNED:
                products.forEach(product -> {
                    product.getInventory().setQuantity(product.getInventory().getQuantity() + totalQuantity);
                    this.inventoryService.update(product.getInventory());
                });
                break;
        }

        order.setStatus(orderStatus);
        this.orderRepository.update(order);
    }

    @Override
    public Order findById(Long id) {
        return this.orderRepository.findById(id);
    }

    @Override
    public void save(Order order) {
        this.orderRepository.save(order);
    }

    @Override
    public void update(Order order) {
        this.orderRepository.update(order);
    }

    @Override
    public void delete(Long id) {
        this.orderRepository.delete(id);
    }

    @Override
    public Long count() {
        return this.orderRepository.count();
    }

    @Override
    public List<Order> findAllWithFilter(Map<String, String> params) {
        return this.orderRepository.findAllWithFilter(params);
    }
}
