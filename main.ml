(* main.ml *)

open Str

(* ------------------------- *)
(* 1. Tipos de Dados         *)
(* ------------------------- *)
type item = {
  id         : int;
  nome       : string;
  marca      : string;
  tipo       : string;
  custo      : float;
  preco      : float;
  quantidade : int;
}

type service = {
  sid         : int;
  nome_s      : string;
  categorias  : string list;
  n_mec       : int;
  tempo       : float;
  custo_base  : float;
}

type lab_cond = Lt of float | Gt of float

(* ------------------------- *)
(* 2. Leitura do Arquivo     *)
(* ------------------------- *)
let read_lines filename =
  let ic = open_in filename in
  let rec loop acc =
    try 
      let line = input_line ic in
      loop (line :: acc)
    with End_of_file -> 
      close_in ic; 
      List.rev acc
  in
  loop []

(* ------------------------- *)
(* 3. Parsers de Linhas      *)
(* ------------------------- *)
let parse_item_line line =
  try
    Scanf.sscanf line
      "item(%d, '%[^']', '%[^']', '%[^']', %f, %f, %d)."
      (fun id nome marca tipo custo preco quant ->
         Some { id; nome; marca; tipo; custo; preco; quantidade = quant })
  with _ -> None

let parse_service_line line =
  try
    Scanf.sscanf line
      "servico(%d, '%[^']', [%[^]]], %d, %f, %f)."
      (fun sid nome cats_str n t cb ->
         let cats = Str.split (Str.regexp ", *") cats_str in
         let cats = List.map (String.trim) cats in
         Some { sid; nome_s = nome; categorias = cats; n_mec = n; tempo = t; custo_base = cb })
  with _ -> None

let parse_discount_line line =
  try
    Scanf.sscanf line
      "desconto_marca('%[^']', %f)."
      (fun m d -> Some (m, d))
  with _ -> None

let parse_lab_discount_line line =
  try Some (Scanf.sscanf line "desconto_mao_obra(<%f, %f)." (fun t d -> (Lt t, d)))
  with _ ->
    (try Some (Scanf.sscanf line "desconto_mao_obra(>%f, %f)." (fun t d -> (Gt t, d)))
     with _ -> None)

let parse_mecanico_line line =
  try
    Scanf.sscanf line
      "mecanico(%d, \"%[^\"]\", %f)."
      (fun id nome custo -> Some (id, nome, custo))
  with _ -> None

(* ------------------------- *)
(* 4. Carregar Dados         *)
(* ------------------------- *)
let items = read_lines "database.pl" |> List.filter_map parse_item_line
let services = read_lines "database.pl" |> List.filter_map parse_service_line
let disc_marca = read_lines "database.pl" |> List.filter_map parse_discount_line
let disc_mao = read_lines "database.pl" |> List.filter_map parse_lab_discount_line
let mecanicos = read_lines "database.pl" |> List.filter_map parse_mecanico_line |> List.map (fun (_, _, c) -> c)

(* ------------------------- *)
(* 5. Funções Auxiliares     *)
(* ------------------------- *)
let discount_of marca =
  try List.assoc marca disc_marca with Not_found -> 0.0

let price_desc it =
  it.preco *. (1.0 -. discount_of it.marca)

let profit_of_item it =
  (price_desc it) -. it.custo

let best_for_category cat =
  items
  |> List.filter (fun it -> it.tipo = cat)
  |> List.fold_left (fun acc it ->
      match acc with
      | None -> Some (it, profit_of_item it)
      | Some (_, p) when profit_of_item it > p -> Some (it, profit_of_item it)
      | _ -> acc
    ) None

let lab_discount_for_hours h =
  List.fold_left (fun acc (cond, d) ->
    match cond with
    | Lt t when h < t -> d
    | Gt t when h > t -> d
    | _ -> acc
  ) 0.0 disc_mao

(* ------------------------- *)
(* 6. Comandos Principais    *)
(* ------------------------- *)
let () =
  match Array.to_list Sys.argv with
  
  (* 6.1 Listar Itens Ordenados *)
  | _ :: "listar_items" :: _ ->
    let sorted = 
      List.sort (fun a b ->
        match compare a.tipo b.tipo with
        | 0 -> (match compare a.marca b.marca with
               | 0 -> compare a.nome b.nome
               | c -> c)
        | c -> c) items
    in
    List.iter (fun it ->
      Printf.printf "%d;%s;%s;%s;%.2f;%.2f;%d\n" 
        it.id it.nome it.marca it.tipo it.custo it.preco it.quantidade
    ) sorted

  (* 6.2 Selecionar Peças para Serviços *)
  | _ :: "orcamento_items" :: ids_str :: _ ->
    let ids = List.map int_of_string (String.split_on_char ',' ids_str) in
    let servicos = List.filter (fun s -> List.mem s.sid ids) services in
    let categorias = List.concat_map (fun s -> s.categorias) servicos |> List.sort_uniq compare in
    categorias |> List.iter (fun cat ->
      match best_for_category cat with
      | Some (it, _) -> 
        Printf.printf "%d;%s;%s;%s;%.2f;%.2f;%d\n" 
          it.id it.nome it.marca it.tipo it.custo (price_desc it) it.quantidade
      | None -> ()
    )

  (* 6.3 Calcular Mão de Obra *)
  | _ :: "orcamento_mecanico" :: ids_str :: _ ->
    let ids = List.map int_of_string (String.split_on_char ',' ids_str) in
    let servicos = List.filter (fun s -> List.mem s.sid ids) services in
    let min_custo = List.fold_left min infinity mecanicos in
    servicos |> List.iter (fun s ->
      let horas = s.tempo *. float_of_int s.n_mec in
      let custo_sem = horas *. min_custo in
      let desc = lab_discount_for_hours horas in
      Printf.printf "%d;%.2f;%.2f;%.2f;%.2f;%.2f\n" 
        s.sid horas min_custo custo_sem desc (custo_sem *. (1.0 -. desc))
    )

  (* 6.4 Descontos em Peças *)
  | _ :: "orcamento_desconto_items" :: ids_str :: _ ->
    let ids = List.map int_of_string (String.split_on_char ',' ids_str) in
    let servicos = List.filter (fun s -> List.mem s.sid ids) services in
    let cats = List.concat_map (fun s -> s.categorias) servicos |> List.sort_uniq compare in
    cats |> List.iter (fun cat ->
      match best_for_category cat with
      | Some (it, _) ->
        let desc = discount_of it.marca *. 100.0 in
        Printf.printf "%d;%.1f%%;%.2f\n" it.id desc (it.preco *. discount_of it.marca)
      | None -> ()
    )

  (* 6.5 Preços Fixos *)
  | _ :: "orcamento_preco_fixo" :: ids_str :: _ ->
    let ids = List.map int_of_string (String.split_on_char ',' ids_str) in
    let servicos = List.filter (fun s -> List.mem s.sid ids) services in
    servicos |> List.iter (fun s ->
      Printf.printf "%d;%.2f\n" s.sid s.custo_base
    )

  (* 6.6 Orçamento Final *)
  | _ :: "orcamento_final" :: ids_str :: _ ->
    let ids = List.map int_of_string (String.split_on_char ',' ids_str) in
    let servicos = List.filter (fun s -> List.mem s.sid ids) services in
    let min_custo = List.fold_left min infinity mecanicos in
    
    let (total_pecas, total_mao, total_fixos, total_desc) = 
      List.fold_left (fun (tp, tm, tf, td) s ->
        (* Peças *)
        let (pecas, desc) = 
          List.fold_left (fun (p, d) cat ->
            match best_for_category cat with
            | Some (it, _) -> 
              (p +. price_desc it, d +. (it.preco *. discount_of it.marca))
            | None -> (p, d)
          ) (0.0, 0.0) s.categorias
        in
        (* Mão de obra *)
        let horas = s.tempo *. float_of_int s.n_mec in
        let custo_mao = horas *. min_custo *. (1.0 -. lab_discount_for_hours horas) in
        (tp +. pecas, tm +. custo_mao, tf +. s.custo_base, td +. desc)
      ) (0.0, 0.0, 0.0, 0.0) servicos
    in
    
    Printf.printf "%.2f;%.2f;%.2f;%.2f;%.2f\n" 
      total_pecas total_mao total_fixos total_desc (total_pecas +. total_mao +. total_fixos -. total_desc)

  (* 6.7 Ajuda *)
  | _ ->
    prerr_endline "Comandos válidos:";
    prerr_endline "  listar_items";
    prerr_endline "  orcamento_items <ids>";
    prerr_endline "  orcamento_mecanico <ids>";
    prerr_endline "  orcamento_desconto_items <ids>";
    prerr_endline "  orcamento_preco_fixo <ids>";
    prerr_endline "  orcamento_final <ids>"