package com.i2i.tenant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.i2i.tenant.config.TenantContext;
import com.i2i.tenant.model.Organization;
import com.i2i.tenant.model.Product;
import com.i2i.tenant.repository.OrganizationRepository;
import com.i2i.tenant.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Product createProduct(Product product) {
        Long organizationId = TenantContext.getOrganization();
        if (organizationId == null) {
            throw new RuntimeException("Organization context is required");
        }
        
        Organization organization = organizationRepository.findById(organizationId)
            .orElseThrow(() -> new RuntimeException("Organization not found"));
            
        product.setOrganization(organization);
        Product savedProduct = productRepository.save(product);
        
        auditLogService.logAction(
            "CREATE",
            "Product",
            savedProduct.getId(),
            String.format("Created product: %s with price: %s", savedProduct.getName(), savedProduct.getPrice())
        );
        
        return savedProduct;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        Long organizationId = TenantContext.getOrganization();
        if (organizationId == null) {
            throw new RuntimeException("Organization context is required");
        }
        List<Product> products = productRepository.findAllByOrganizationId(organizationId);
        
        // Log each product access individually
        for (Product product : products) {
            auditLogService.logAction(
                "READ",
                "Product",
                product.getId(),
                String.format("Retrieved product: %s", product.getName())
            );
        }
        
        return products;
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        Long organizationId = TenantContext.getOrganization();
        if (organizationId == null) {
            throw new RuntimeException("Organization context is required");
        }
        Optional<Product> product = productRepository.findByIdAndOrganizationId(id, organizationId);
        
        if (product.isPresent()) {
            auditLogService.logAction(
                "READ",
                "Product",
                id,
                String.format("Retrieved product: %s", product.get().getName())
            );
        }
        
        return product;
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        Long organizationId = TenantContext.getOrganization();
        if (organizationId == null) {
            throw new RuntimeException("Organization context is required");
        }
        
        Product existingProduct = productRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update only the fields that are provided
        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }

        Product updatedProduct = productRepository.save(existingProduct);
        
        auditLogService.logAction(
            "UPDATE",
            "Product",
            id,
            String.format("Updated product: %s", updatedProduct.getName())
        );
        
        return updatedProduct;
    }

    @Transactional
    public void deleteProduct(Long id) {
        Long organizationId = TenantContext.getOrganization();
        if (organizationId == null) {
            throw new RuntimeException("Organization context is required");
        }
        
        Product product = productRepository.findByIdAndOrganizationId(id, organizationId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
                
        productRepository.deleteById(id);
        
        auditLogService.logAction(
            "DELETE",
            "Product",
            id,
            String.format("Deleted product: %s", product.getName())
        );
    }
} 