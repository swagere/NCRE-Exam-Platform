package com.group.auto_generating_exam.config.JudgeConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RunConfig {
    String command;
    String seccomp_rule;
    String[] env;
    int memory_limit_check_only;
}
