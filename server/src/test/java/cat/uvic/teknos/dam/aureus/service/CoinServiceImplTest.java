package cat.uvic.teknos.dam.aureus.service;

import cat.uvic.teknos.dam.aureus.impl.CoinImpl;
import cat.uvic.teknos.dam.aureus.service.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CoinServiceImplTest {

    @Test
    void seedDataHasAtLeastTwoCoins() {
        CoinServiceImpl svc = new CoinServiceImpl();
        List<CoinImpl> all = svc.findAll();
        assertTrue(all.size() >= 2, "Seed data should contain at least two coins");
    }

    @Test
    void createAssignsIdWhenMissing() {
        CoinServiceImpl svc = new CoinServiceImpl();
        CoinImpl c = new CoinImpl();
        c.setCoinName("TestCoin");
        c.setCoinYear(2025);
        c.setId(null);

        CoinImpl created = svc.create(c);
        assertNotNull(created.getId(), "Created coin must have an id");
        assertEquals("TestCoin", created.getCoinName());
    }

    @Test
    void findByIdUpdateAndDeleteBehavior() {
        CoinServiceImpl svc = new CoinServiceImpl();
        CoinImpl c = new CoinImpl();
        c.setCoinName("ToModify");
        c.setCoinYear(1);
        CoinImpl created = svc.create(c);

        CoinImpl fetched = svc.findById(created.getId());
        assertEquals(created.getCoinName(), fetched.getCoinName());

        // Update
        created.setCoinName("Modified");
        svc.update(created);
        CoinImpl updated = svc.findById(created.getId());
        assertEquals("Modified", updated.getCoinName());

        // Delete
        svc.delete(created.getId());
        assertThrows(EntityNotFoundException.class, () -> svc.findById(created.getId()));
    }

    @Test
    void updateThrowsWhenIdMissing() {
        CoinServiceImpl svc = new CoinServiceImpl();
        CoinImpl c = new CoinImpl();
        c.setCoinName("NoId");
        c.setCoinYear(0);
        c.setId(null);

        assertThrows(IllegalArgumentException.class, () -> svc.update(c));
    }
}

