package ua.procamp;

import lombok.extern.slf4j.Slf4j;
import ua.procamp.dao.ProductDao;
import ua.procamp.dao.ProductDaoImpl;
import ua.procamp.exception.DaoOperationException;
import ua.procamp.model.Product;
import ua.procamp.util.JdbcUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.Assert.*;

@Slf4j
@RunWith(JUnit4.class)
public class ProductDaoTest {
    private static ProductDao productDao;

    @BeforeClass
    public static void init() throws SQLException {
        DataSource h2DataSource = JdbcUtil.createDefaultInMemoryH2DataSource();
        createAccountTable(h2DataSource);
        productDao = new ProductDaoImpl(h2DataSource);
    }

    private static void createAccountTable(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            Statement createTableStatement = connection.createStatement();
            createTableStatement.execute("CREATE TABLE IF NOT EXISTS products (\n" +
                    "  id            SERIAL NOT NULL,\n" +
                    "  name     VARCHAR(255) NOT NULL,\n" +
                    "  producer     VARCHAR(255) NOT NULL,\n" +
                    "  price       DECIMAL(19, 4),\n" +
                    "  expiration_date      TIMESTAMP NOT NULL,\n" +
                    "  creation_time TIMESTAMP NOT NULL DEFAULT now(),\n" +
                    "\n" +
                    "  CONSTRAINT products_pk PRIMARY KEY (id)\n" +
                    ");\n" +
                    "\n");
        }
    }

    private Product generateTestProduct() {
        return Product.builder()
                .name(RandomStringUtils.randomAlphabetic(10))
                .producer(RandomStringUtils.randomAlphabetic(20))
                .price(BigDecimal.valueOf(RandomUtils.nextInt(10, 100)))
                .expirationDate(LocalDate.ofYearDay(LocalDate.now().getYear() + RandomUtils.nextInt(1, 5),
                        RandomUtils.nextInt(1, 365)))
                .build();
    }

    @Test
    public void testSave() {
        Product fanta = createTestFantaProduct();

        int productsCountBeforeInsert = productDao.findAll().size();
        productDao.save(fanta);
        log.info("fanta={}", fanta);
        List<Product> products = productDao.findAll();

        assertNotNull(fanta.getId());
        assertEquals(productsCountBeforeInsert + 1, products.size());
        assertTrue(products.contains(fanta));
    }

    @Test
    public void testSaveInvalidProduct() {
        Product invalidTestProduct = createInvalidTestProduct();

        try {
            productDao.save(invalidTestProduct);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertEquals(DaoOperationException.class, e.getClass());
            assertEquals(String.format("Error saving product: %s", invalidTestProduct), e.getMessage());
        }
    }

    private Product createTestFantaProduct() {
        return Product.builder()
                .name("Fanta")
                .producer("The Coca-Cola Company")
                .price(BigDecimal.valueOf(22))
                .expirationDate(LocalDate.of(2020, Month.APRIL, 14)).build();
    }

    private Product createInvalidTestProduct() {
        return Product.builder()
                .name("INVALID")
                .price(BigDecimal.valueOf(22))
                .expirationDate(LocalDate.of(2020, Month.APRIL, 14)).build();
    }


    @Test
    public void testFindAll() {
        List<Product> newProducts = createTestProductList();
        List<Product> oldProducts = productDao.findAll();
        newProducts.forEach(productDao::save);

        log.info("new={},  old={}", newProducts, oldProducts);

        List<Product> products = productDao.findAll();
        log.info("products={}", products);

        assertTrue(products.containsAll(newProducts));
        assertTrue(products.containsAll(oldProducts));
        assertEquals(oldProducts.size() + newProducts.size(), products.size());

    }

    private List<Product> createTestProductList() {
        return List.of(
                Product.builder()
                        .name("Sprite")
                        .producer("The Coca-Cola Company")
                        .price(BigDecimal.valueOf(18))
                        .expirationDate(LocalDate.of(2020, Month.MARCH, 24)).build(),
                Product.builder()
                        .name("Cola light")
                        .producer("The Coca-Cola Company")
                        .price(BigDecimal.valueOf(21))
                        .expirationDate(LocalDate.of(2020, Month.JANUARY, 11)).build(),
                Product.builder()
                        .name("Snickers")
                        .producer("Mars Inc.")
                        .price(BigDecimal.valueOf(16))
                        .expirationDate(LocalDate.of(2019, Month.DECEMBER, 3)).build()
        );
    }

    @Test
    public void testFindById() {
        Product testProduct = generateTestProduct();
        productDao.save(testProduct);

        Product product = productDao.findOne(testProduct.getId());

        assertEquals(testProduct, product);
        assertEquals(testProduct.getName(), product.getName());
        assertEquals(testProduct.getProducer(), product.getProducer());
        assertEquals(testProduct.getPrice().setScale(2), product.getPrice().setScale(2));
        assertEquals(testProduct.getExpirationDate(), product.getExpirationDate());
    }

    @Test
    public void testFindByNotExistingId() {
        long invalidId = -1L;
        try {
            productDao.findOne(invalidId);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertEquals(DaoOperationException.class, e.getClass());
            assertEquals(String.format("Product with id = %d does not exist", invalidId), e.getMessage());
        }
    }

    @Test
    public void testUpdate() {
        Product testProduct = generateTestProduct();
        productDao.save(testProduct);
        List<Product> productsBeforeUpdate = productDao.findAll();

        testProduct.setName("Updated name");
        testProduct.setProducer("Updated producer");
        testProduct.setPrice(BigDecimal.valueOf(666));
        testProduct.setExpirationDate(LocalDate.of(2020, Month.JANUARY, 1));
        productDao.update(testProduct);
        List<Product> products = productDao.findAll();
        Product updatedProduct = productDao.findOne(testProduct.getId());

        log.info("test={}, upd={}", testProduct, updatedProduct);

        assertEquals(productsBeforeUpdate.size(), products.size());
        assertTrue(completelyEquals(testProduct, updatedProduct));
        productsBeforeUpdate.remove(testProduct);
        products.remove(testProduct);
        assertTrue(deepEquals(productsBeforeUpdate, products));
    }

    private boolean completelyEquals(Product productBeforeUpdate, Product productAfterUpdate) {
        return productBeforeUpdate.getName().equals(productAfterUpdate.getName())
                && productBeforeUpdate.getProducer().equals(productAfterUpdate.getProducer())
                && productBeforeUpdate.getPrice().setScale(2).equals(productAfterUpdate.getPrice().setScale(2))
                && productBeforeUpdate.getExpirationDate().equals(productAfterUpdate.getExpirationDate());
    }

    private boolean deepEquals(List<Product> productsBeforeUpdate, List<Product> productsAfterUpdate) {
        return productsAfterUpdate.stream()
                .allMatch(product -> remainedTheSame(product, productsBeforeUpdate));
    }

    private boolean remainedTheSame(Product productAfterUpdate, List<Product> productsBeforeUpdate) {
        Product productBeforeUpdate = findById(productsBeforeUpdate, productAfterUpdate.getId());
        return completelyEquals(productAfterUpdate, productBeforeUpdate);
    }

    private Product findById(List<Product> products, Long id) {
        return products.stream().filter(p -> p.getId().equals(id)).findFirst().get();
    }

    @Test
    public void testUpdateNotStored() {
        Product notStoredProduct = generateTestProduct();

        try {
            productDao.update(notStoredProduct);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertEquals(DaoOperationException.class, e.getClass());
            assertEquals("Product id cannot be null", e.getMessage());
        }
    }

    @Test
    public void testUpdateProductWithInvalidId() {
        Product testProduct = generateTestProduct();
        long invalidId = -1L;
        testProduct.setId(invalidId);

        try {
            productDao.update(testProduct);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertEquals(DaoOperationException.class, e.getClass());
            assertEquals(String.format("Product with id = %d does not exist", invalidId), e.getMessage());
        }
    }

    @Test
    public void testRemove() {
        Product testProduct = generateTestProduct();
        productDao.save(testProduct);
        List<Product> productsBeforeRemove = productDao.findAll();

        productDao.remove(testProduct);
        List<Product> products = productDao.findAll();

        assertEquals(productsBeforeRemove.size() - 1, products.size());
        assertFalse(products.contains(testProduct));
    }

    @Test
    public void testRemoveNotStored() {
        Product notStoredProduct = generateTestProduct();

        try {
            productDao.remove(notStoredProduct);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertEquals(DaoOperationException.class, e.getClass());
            assertEquals("Product id cannot be null", e.getMessage());
        }
    }

    @Test
    public void testRemoveProductWithInvalidId() {
        Product testProduct = generateTestProduct();
        long invalidId = -1L;
        testProduct.setId(invalidId);

        try {
            productDao.remove(testProduct);
            fail("Exception was't thrown");
        } catch (Exception e) {
            assertEquals(DaoOperationException.class, e.getClass());
            assertEquals(String.format("Product with id = %d does not exist", invalidId), e.getMessage());
        }
    }
}