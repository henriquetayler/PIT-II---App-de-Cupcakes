package br.com.cupcakes.repository;

import br.com.cupcakes.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findByUsuarioIdOrderByDataHoraDesc(Long usuarioId);

    List<Pedido> findAllByOrderByDataHoraDesc();
}
