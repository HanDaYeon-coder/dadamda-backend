package com.forever.dadamda.controller;

import static com.forever.dadamda.entity.board.TAG.LIFE_SHOPPING;
import static com.forever.dadamda.service.UUIDService.generateUUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forever.dadamda.dto.board.CreateBoardRequest;
import com.forever.dadamda.dto.board.UpdateBoardRequest;
import com.forever.dadamda.entity.board.Board;
import com.forever.dadamda.entity.user.User;
import com.forever.dadamda.mock.WithCustomMockUser;
import com.forever.dadamda.repository.UserRepository;
import com.forever.dadamda.repository.board.BoardRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
@Sql(scripts = "/board-setup.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
public class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    UUID board2UUID = UUID.fromString("30373832-6566-3438-2d61-3433392d3132");

    UUID board1UUID = UUID.fromString("30373832-6566-3438-2d61-3433392d3131");

    UUID notExistentboardUUID = UUID.fromString("30373832-6566-3438-2d61-3433392d3001");

    @Test
    @WithCustomMockUser
    public void should_it_created_normally_When_creating_board_with_the_title_name_and_tag_entered()
            throws Exception {
        //타이틀, 이름, 태그를 입력한 보드를 만들 때, 정상적으로 만들어지는지 확인
        //given
        boardRepository.deleteAll();

        CreateBoardRequest createBoardRequest = CreateBoardRequest.builder()
                .tag("ENTERTAINMENT_ART")
                .title("board test1")
                .description("board test2")
                .build();
        String content = objectMapper.writeValueAsString(createBoardRequest);

        //when
        //then
        mockMvc.perform(post("/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithCustomMockUser
    public void should_it_returns_4xx_error_When_creating_board_with_a_tag_that_does_not_exist()
            throws Exception {
        //존재하지 않는 태그 입력할 때, 4xx 에러를 반환하는지 확인
        //given
        CreateBoardRequest createBoardRequest = CreateBoardRequest.builder()
                .tag("ENTERTAINMENT_ARTIST")
                .title("test")
                .description("test")
                .build();
        String content = objectMapper.writeValueAsString(createBoardRequest);

        //when
        //then
        mockMvc.perform(post("/v1/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                        .content(content)
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError());
    }

    @Test
    @WithCustomMockUser
    public void should_it_returns_4xx_error_When_deleting_a_board_that_does_not_exist()
            throws Exception {
        //존재하지 않는 보드를 삭제할 때 4xx에러를 반환하는지 확인
        mockMvc.perform(delete("/v1/boards/{boardUUID}", notExistentboardUUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.jsonPath("$.resultCode").value("NF005"));
    }

    @Test
    @WithCustomMockUser
    public void should_it_is_deleted_successfully_When_deleting_an_existing_board()
            throws Exception {
        //존재하는 보드를 삭제할 때 성공적으로 삭제되는지 확인
        mockMvc.perform(delete("/v1/boards/{boardUUID}", board2UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithCustomMockUser
    public void should_it_is_fixed_successfully_When_fixing_an_existing_board()
            throws Exception {
        //존재하는 보드를 고정할 때, 성공적으로 고정되는지 확인
        mockMvc.perform(patch("/v1/boards/{boardUUID}/fix", board2UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithCustomMockUser
    public void should_it_returns_the_fixed_date_depending_on_whether_the_board_is_fixed_or_not_When_getting_a_list_of_boards()
            throws Exception {
        //보드 목록을 조회할 떄, 고정된 날짜가 있는 경우 날짜를 반환하는지 확인
        mockMvc.perform(get("/v1/boards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].isFixed").value("2023-01-04T11:11:01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[2].isFixed").doesNotExist());
    }

    @Test
    @WithCustomMockUser
    public void should_board_tag_is_returned_to_english_When_getting_a_list_of_boards()
            throws Exception {
        //보드 목록을 조회할 떄, tag가 영어로 반환되는지 확인
        mockMvc.perform(get("/v1/boards")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].tag").value("ENTERTAINMENT_ART"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[1].tag").value("LIFE_SHOPPING"));
    }

    @Test
    @WithCustomMockUser
    public void should_it_is_modified_successfully_When_modifying_the_board()
            throws Exception {
        //보드 수정할 때, 성공적으로 수정되는지 확인
        //given
        UpdateBoardRequest updateBoardRequest = UpdateBoardRequest.builder()
                .tag("LIFE_SHOPPING")
                .title("test")
                .description("test123")
                .build();
        String content = objectMapper.writeValueAsString(updateBoardRequest);

        //when
        //then
        mockMvc.perform(patch("/v1/boards/{boardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
  
    @Test
    @WithCustomMockUser
    public void should_count_is_returned_successfully_When_getting_the_number_of_boards()
            throws Exception {
        // 보드 개수 조회할 때, count가 성공적으로 반환되는지 확인
        mockMvc.perform(get("/v1/boards/count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.count").exists());
    }

    @Test
    @WithCustomMockUser
    public void should_the_board_name_description_and_tag_are_successfully_returned_When_getting_board_individually()
            throws Exception {
        // 보드 개별 조회할 때, 성공적으로 보드명, 설명, 태그가 반환된다.
        mockMvc.perform(get("/v1/boards/{boardUUID}", board1UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.boardId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.title").value("board1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.description").value("test"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.tag").value("ENTERTAINMENT_ART"));
    }

    @Test
    @WithCustomMockUser
    public void should_the_name_fixed_date_uuid_and_tag_are_returned_successfully_When_searching_for_a_board()
            throws Exception {
        // 보드를 검색할 때, 이름, 고정된 날짜, uuid, 태그가 성공적으로 출력된다.
        // given
        boardRepository.deleteAll();

        UUID boardUUID = generateUUID();
        User user = userRepository.findById(1L).get();
        Board board = Board.builder()
                .isPublic(true)
                .title("board10")
                .description("test")
                .tag(LIFE_SHOPPING)
                .fixedDate(LocalDateTime.of(2023, 1, 30, 11, 11, 1))
                .uuid(boardUUID)
                .user(user)
                .build();
        boardRepository.save(board);

        //when
        //then
        mockMvc.perform(get("/v1/boards/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-AUTH-TOKEN", "aaaaaaa")
                        .param("keyword", "board10")
                        .param("page", "0")
                        .param("size", "10")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].title").value("board10"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].description").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].isFixed").value("2023-01-30T11:11:01"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].uuid").value(boardUUID.toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.content[0].tag").value("LIFE_SHOPPING"));
    }
}
